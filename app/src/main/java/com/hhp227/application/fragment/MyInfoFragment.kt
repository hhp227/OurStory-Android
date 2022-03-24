package com.hhp227.application.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.activity.ImageSelectActivity
import com.hhp227.application.activity.ImageSelectActivity.Companion.SELECT_TYPE
import com.hhp227.application.activity.ImageSelectActivity.Companion.SINGLE_SELECT_TYPE
import com.hhp227.application.activity.MyInfoActivity
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentMyinfoBinding
import com.hhp227.application.dto.UserItem
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.util.DateUtil
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.MyInfoViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MyInfoFragment : Fragment() {
    private lateinit var snackbar: Snackbar

    private val viewModel: MyInfoViewModel by viewModels {
        InjectorUtils.provideMyInfoViewModelFactory()
    }

    private val permissionRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Intent(requireContext(), ImageSelectActivity::class.java).also { intent ->
                intent.putExtra(SELECT_TYPE, SINGLE_SELECT_TYPE)
                cameraPickImageActivityResultLauncher.launch(intent)
            }
        }
    }

    private val cameraCaptureImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            val bitmap = try {
                BitmapUtil(requireContext()).bitmapResize(viewModel.photoURI, 200)?.let {
                    val ei = viewModel.currentPhotoPath.let(::ExifInterface)
                    val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                    return@let BitmapUtil(requireContext()).rotateImage(it, when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90F
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180F
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270F
                        else -> 0F
                    })
                }
            } catch (e: IOException) {
                null
            }

            viewModel.setBitmapFlow(bitmap)
        }
    }

    private val cameraPickImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.data != null) {
            val bitmap = BitmapUtil(requireContext()).bitmapResize(result.data?.data, 200)

            viewModel.setBitmapFlow(bitmap)
        }
    }

    private var binding: FragmentMyinfoBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyinfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivProfileImage.setOnClickListener {
            registerForContextMenu(it)
            requireActivity().openContextMenu(it)
            unregisterForContextMenu(it)
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.imageUrl != null -> {
                    hideProgressBar()
                    requireActivity().setResult(RESULT_OK)
                    viewModel.updateUserDataStore(state.imageUrl)
                    Snackbar.make(requireView(), getString(R.string.update_complete), Snackbar.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    Snackbar.make(requireView(), state.error, Snackbar.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
        // TODO 좀더 고쳐야할 필요가 있음
        combine(viewModel.bitmapFlow, viewModel.userFlow) { bitmap: Bitmap?, user: UserItem? ->
            if (user != null) {
                binding.tvName.text = user.name
                binding.tvEmail.text = user.email
                binding.tvCreateAt.text = "${DateUtil.getPeriodTimeGenerator(requireContext(), user.createAt)} 가입"

                /*Glide.with(this@MyInfoFragment)
                    .load(URLs.URL_USER_PROFILE_IMAGE + user.profileImage)
                    .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                    .into(binding.ivProfileImage)*/
            }
            if (bitmap != null) {
                /*Glide.with(this@MyInfoFragment)
                    .load(bitmap ?: ResourcesCompat.getDrawable(resources, R.drawable.profile_img_circle, null))
                    .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                    .into(binding.ivProfileImage)*/
                (requireActivity() as? MyInfoActivity)?.inflateMenu {
                    showProgressBar()
                    viewModel.uploadImage()
                }
                Log.e("TEST", "bitmapFlow: $bitmap")
            }
            Glide.with(this@MyInfoFragment)
                .load(
                    when {
                        bitmap != null -> bitmap
                        user != null -> URLs.URL_USER_PROFILE_IMAGE + user.profileImage
                        else -> ResourcesCompat.getDrawable(resources, R.drawable.profile_img_circle, null)
                    }
                )
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(binding.ivProfileImage)
        }.launchIn(lifecycleScope)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle(R.string.context_profile_change_title)
        activity?.menuInflater?.inflate(R.menu.myinfo, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.album -> {
            permissionRequestLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            true
        }
        R.id.camera -> {
            File.createTempFile(
                "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}_", /* prefix */
                ".jpg", /* suffix */
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ).also { file ->
                viewModel.photoURI = FileProvider.getUriForFile(requireContext(), requireContext().packageName, file)
                viewModel.currentPhotoPath = file.absolutePath
            }
            cameraCaptureImageActivityResultLauncher.launch(viewModel.photoURI)
            true
        }
        R.id.remove -> {
            viewModel.setBitmapFlow(null)
            true
        }
        else -> false
    }

    private fun showProgressBar() {
        Snackbar.make(requireView(), getString(R.string.sending), Snackbar.LENGTH_INDEFINITE).let {
            snackbar = it

            it.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup
        }.also {
            it.addView(ProgressBar(requireContext()))
        }
        if (!snackbar.isShown)
            snackbar.show()
    }

    private fun hideProgressBar() = snackbar.takeIf(Snackbar::isShown)?.apply { dismiss() }

    companion object {
        fun newInstance(): Fragment = MyInfoFragment()
    }
}