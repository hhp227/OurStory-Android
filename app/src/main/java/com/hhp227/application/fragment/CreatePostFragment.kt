package com.hhp227.application.fragment

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.activity.ImageSelectActivity
import com.hhp227.application.activity.ImageSelectActivity.Companion.MULTI_SELECT_TYPE
import com.hhp227.application.activity.ImageSelectActivity.Companion.SELECT_TYPE
import com.hhp227.application.adapter.WriteListAdapter
import com.hhp227.application.databinding.FragmentCreatePostBinding
import com.hhp227.application.dto.ListItem
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.CreatePostViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.IOException

class CreatePostFragment : Fragment() {
    private lateinit var snackbar: Snackbar

    private var binding: FragmentCreatePostBinding by autoCleared()

    private val viewModel: CreatePostViewModel by viewModels {
        InjectorUtils.provideCreatePostViewModelFactory(this)
    }

    private val cameraCaptureImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            val bitmap = try {
                viewModel.photoURI?.let { uri ->
                    val ei = requireContext().contentResolver.openInputStream(uri)?.let(::ExifInterface)
                    val orientation = ei?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                    return@let BitmapUtil(requireContext()).bitmapResize(uri, 200)?.let {
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
        result.data?.getParcelableArrayExtra("data")?.forEach { uri ->
            val bitmap = BitmapUtil(requireContext()).bitmapResize(uri as Uri, 200)

            viewModel.setBitmap(bitmap)
        }
    }

    private val requestPermissionsResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Intent(requireContext(), ImageSelectActivity::class.java).putExtra(SELECT_TYPE, MULTI_SELECT_TYPE)
                .also(cameraPickImageActivityResultLauncher::launch)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = WriteListAdapter().apply {
            setOnWriteListAdapterListener(object : WriteListAdapter.OnWriteListAdapterListener {
                override fun onItemClick(v: View, p: Int) {
                    v.setOnCreateContextMenuListener { menu, _, _ ->
                        menu.apply {
                            setHeaderTitle(getString(R.string.select_action))
                            add(0, p, Menu.NONE, getString(R.string.delete))
                        }
                    }
                    v.showContextMenu()
                }

                override fun onValueChange(e: Editable?) {
                    viewModel.text.value = e.toString()
                }
            })
        }

        binding.toolbar.apply {
            setupWithNavController(findNavController())
            inflateMenu(R.menu.write)
            setOnMenuItemClickListener(::onOptionsItemSelected)
        }
        binding.ibImage.setOnClickListener(::showContextMenu)
        binding.ibVideo.setOnClickListener(::showContextMenu)
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { state ->
                when {
                    state.isLoading -> {
                        showProgressBar()
                    }
                    state.postId >= 0 -> {
                        hideProgressBar()
                        setFragmentResult(findNavController().previousBackStackEntry?.destination?.displayName ?: "", bundleOf())
                        findNavController().navigateUp()
                    }
                    state.itemList.isNotEmpty() -> {
                        (binding.recyclerView.adapter as WriteListAdapter).submitList(state.itemList)
                    }
                    state.error.isNotBlank() -> {
                        hideProgressBar()
                        Snackbar.make(requireView(), state.error, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .launchIn(lifecycleScope)
        viewModel.textFormState
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { state ->
                state.textError?.let { error ->
                    (binding.recyclerView.findViewHolderForAdapterPosition(0) as? WriteListAdapter.WriteViewHolder.HeaderHolder)?.binding?.etText?.error = getString(error)
                }
            }
            .launchIn(lifecycleScope)
        viewModel.bitmapFlow
            .onEach { bitmap ->
                if (bitmap != null) {
                    viewModel.addItem(ListItem.Image(bitmap = bitmap))
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_send -> {
            viewModel.actionSend()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu.apply {
            when (v.id) {
                R.id.ib_image -> {
                    setHeaderTitle(getString(R.string.context_image_title))
                    add(1, Menu.NONE, Menu.NONE, getString(R.string.gallery))
                    add(2, Menu.NONE, Menu.NONE, getString(R.string.camera))
                }
                R.id.ib_video -> {
                    setHeaderTitle(getString(R.string.context_video_title))
                    add(3, Menu.NONE, Menu.NONE, getString(R.string.youtube))
                }
                else -> super.onCreateContextMenu(menu, v, menuInfo)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.groupId) {
        0 -> {
            viewModel.removeItem(item.itemId)
            true
        }
        1 -> {
            requestPermissionsResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            true
        }
        2 -> {
            cameraCaptureImageActivityResultLauncher.launch(viewModel.getUriToSaveImage())
            true
        }
        3 -> {
            Toast.makeText(requireContext(), "동영상 선택", Toast.LENGTH_LONG).show()
            true
        }
        else -> super.onContextItemSelected(item)
    }

    private fun showContextMenu(v: View) {
        registerForContextMenu(v)
        requireActivity().openContextMenu(v)
        unregisterForContextMenu(v)
    }

    private fun showProgressBar() {
        snackbar = Snackbar.make(requireView(), getString(R.string.sending), Snackbar.LENGTH_INDEFINITE).also {
            (it.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup).addView(ProgressBar(requireContext()))
        }

        if (!snackbar.isShown)
            snackbar.show()
    }

    private fun hideProgressBar() = snackbar.takeIf { it.isShown }?.run { dismiss() }
}