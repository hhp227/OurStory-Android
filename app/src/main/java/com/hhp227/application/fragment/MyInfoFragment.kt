package com.hhp227.application.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isEmpty
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
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.data.UserRepository
import com.hhp227.application.databinding.FragmentMyinfoBinding
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.util.DateUtil
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.MyInfoViewModel
import com.hhp227.application.viewmodel.MyInfoViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MyInfoFragment : Fragment() {
    private val viewModel: MyInfoViewModel by viewModels {
        MyInfoViewModelFactory(UserRepository())
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
            viewModel.setBitmap(BitmapUtil(requireContext()))
            setProfileImageView()
        }
    }

    private val cameraPickImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.data != null) {
            viewModel.bitmap = BitmapUtil(requireContext()).bitmapResize(result.data?.data, 200)

            setProfileImageView()
        }
    }

    private var binding: FragmentMyinfoBinding by autoCleared()

    private lateinit var snackbar: Snackbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyinfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            AppController.getInstance().preferenceManager.user?.also { user ->
                tvName.text = user.name
                tvEmail.text = user.email
                tvCreateAt.text = "${DateUtil.getPeriodTimeGenerator(requireContext(), user.createAt)} 가입"

                Glide.with(this@MyInfoFragment)
                    .load(URLs.URL_USER_PROFILE_IMAGE + user.profileImage)
                    .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                    .into(ivProfileImage)
            }
            ivProfileImage.setOnClickListener {
                registerForContextMenu(it)
                requireActivity().openContextMenu(it)
                unregisterForContextMenu(it)
            }
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.imageUrl != null -> {
                    hideProgressBar()
                    requireActivity().setResult(RESULT_OK)
                    AppController.getInstance().preferenceManager.also { pm ->
                        pm.user?.let { user -> pm.storeUser(user.apply { profileImage = state.imageUrl }) }
                    }
                    parentFragmentManager.fragments.forEach { fragment -> if (fragment is MyPostFragment) fragment.profileUpdateResult() }
                    Snackbar.make(requireView(), getString(R.string.update_complete), Snackbar.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    Snackbar.make(requireView(), state.error, Snackbar.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle("프로필 이미지 변경")
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
            viewModel.bitmap = null

            setProfileImageView()
            true
        }
        else -> false
    }

    private fun inflateMenu(action: () -> Unit) {
        (requireActivity() as MyInfoActivity).also { activity ->
            if (activity.binding.toolbar.menu.isEmpty()) {
                activity.menuInflater.inflate(R.menu.save, activity.binding.toolbar.menu)
            }
            activity.binding.toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.save -> {
                        action.invoke()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setProfileImageView() {
        Glide.with(this@MyInfoFragment)
            .load(viewModel.bitmap ?: resources.getDrawable(R.drawable.profile_img_circle, null))
            .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
            .into(binding.ivProfileImage)
        inflateMenu {
            showProgressBar()
            viewModel.uploadImage()
        }
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