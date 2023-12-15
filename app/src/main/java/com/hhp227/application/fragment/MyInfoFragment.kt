package com.hhp227.application.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.activity.ImageSelectActivity
import com.hhp227.application.activity.ImageSelectActivity.Companion.SELECT_TYPE
import com.hhp227.application.activity.ImageSelectActivity.Companion.SINGLE_SELECT_TYPE
import com.hhp227.application.util.URLs
import com.hhp227.application.databinding.FragmentMyinfoBinding
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.util.DateUtil
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.MyInfoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.IOException

// WIP
class MyInfoFragment : Fragment() {
    private val snackbar: Snackbar by lazy {
        Snackbar.make(requireView(), getString(R.string.sending), Snackbar.LENGTH_INDEFINITE)
            .apply {
                (view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup)
                    .addView(ProgressBar(requireContext()))
            }
    }

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
                viewModel.photoURI?.let { uri ->
                    BitmapUtil(requireContext()).bitmapResize(uri, 200)?.let {
                        val ei = requireContext().contentResolver.openInputStream(uri)?.let(::ExifInterface)
                        val orientation = ei?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

                        BitmapUtil(requireContext()).rotateImage(it, when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> 90F
                            ExifInterface.ORIENTATION_ROTATE_180 -> 180F
                            ExifInterface.ORIENTATION_ROTATE_270 -> 270F
                            else -> 0F
                        })
                    }
                }
            } catch (e: IOException) {
                null
            }

            viewModel.setBitmap(bitmap)
        }
    }

    private val cameraPickImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.data != null) {
            val bitmap = BitmapUtil(requireContext()).bitmapResize(result.data?.data, 200)

            viewModel.setBitmap(bitmap)
        }
    }

    private var binding: FragmentMyinfoBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyinfoBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivProfileImage.setOnClickListener {
            registerForContextMenu(it)
            requireActivity().openContextMenu(it)
            unregisterForContextMenu(it)
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when {
                state.isLoading -> showProgressBar()
                /*state.userInfo != null -> {
                    hideProgressBar()
                    requireActivity().setResult(RESULT_OK)
                    viewModel.updateUserDataStore(state.userInfo)
                    Snackbar.make(requireView(), getString(R.string.update_complete), Snackbar.LENGTH_LONG).show()
                    viewModel.resetState()
                }*/
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    Snackbar.make(requireView(), state.error, Snackbar.LENGTH_LONG).show()
                }
            }
        }
        /*combine(viewModel.imageHolder, viewModel.userFlow) { holder, user ->
            if (user != null) {
                binding.tvName.text = user.name
                binding.tvEmail.text = user.email
                binding.tvCreateAt.text = "${DateUtil.getPeriodTimeGenerator(requireContext(), user.createdAt)} 가입"

                if (holder.imageUrl == null) {
                    (parentFragment as? ProfileFragment)?.inflateMenu {
                        showProgressBar()
                        viewModel.uploadImage()
                    }
                }
                binding.ivProfileImage.load(when {
                    holder.bitmap != null -> holder.bitmap
                    holder.imageUrl != null -> URLs.URL_USER_PROFILE_IMAGE + holder.imageUrl
                    else -> ResourcesCompat.getDrawable(resources, R.drawable.profile_img_circle, null)
                }) {
                    placeholder(R.drawable.profile_img_circle)
                    error(R.drawable.profile_img_circle)
                    transformations(CircleCropTransformation())
                }
            }
        }
            .launchIn(lifecycleScope)*/
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle(R.string.context_profile_change_title)
        activity?.menuInflater?.inflate(R.menu.my_info, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.album -> {
            permissionRequestLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            true
        }
        R.id.camera -> {
            cameraCaptureImageActivityResultLauncher.launch(viewModel.getUriToSaveImage())
            true
        }
        R.id.remove -> {
            viewModel.setBitmap(null)
            true
        }
        else -> false
    }

    private fun showProgressBar() {
        if (!snackbar.isShown)
            snackbar.show()
    }

    private fun hideProgressBar() = snackbar.takeIf(Snackbar::isShown)?.run { dismiss() }

    companion object {
        fun newInstance(): Fragment = MyInfoFragment()
    }
}