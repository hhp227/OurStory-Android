package com.hhp227.application.fragment

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
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
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.model.ListItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.CreatePostViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

class CreatePostFragment : Fragment(), MenuProvider {
    private val snackbar: Snackbar by lazy {
        Snackbar.make(requireView(), getString(R.string.sending), Snackbar.LENGTH_INDEFINITE).apply {
            (view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup).addView(ProgressBar(requireContext()))
        }
    }

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

            viewModel.addItem(ListItem.Image(bitmap = bitmap))
        }
    }

    private val cameraPickImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getParcelableArrayExtra("data")?.forEach { uri ->
            val bitmap = BitmapUtil(requireContext()).bitmapResize(uri as Uri, 200)

            viewModel.addItem(ListItem.Image(bitmap = bitmap))
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
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
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
                    viewModel.state.value = viewModel.state.value?.copy(text = e.toString())
                }
            })
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNavAppBar(binding.toolbar)
        binding.ibImage.setOnClickListener(::showContextMenu)
        binding.ibVideo.setOnClickListener(::showContextMenu)
        showInputMethod()
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when {
                state.textError != null -> {
                    (binding.recyclerView.findViewHolderForAdapterPosition(0) as? WriteListAdapter.WriteViewHolder.HeaderHolder)?.binding?.etText?.error = getString(state.textError)
                }
                state.isLoading -> {
                    showProgressBar()
                }
                state.postId >= 0 -> {
                    hideProgressBar()
                    setFragmentResult(findNavController().previousBackStackEntry?.destination?.displayName ?: "", bundleOf())
                    findNavController().navigateUp()
                }
                state.message.isNotBlank() -> {
                    hideProgressBar()
                    Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(this)
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

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.write, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.action_send) {
            viewModel.actionSend(viewModel.state.value!!.text, viewModel.state.value!!.itemList)
        }
        return false
    }

    private fun setNavAppBar(toolbar: Toolbar) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).addMenuProvider(this)
        toolbar.setupWithNavController(findNavController())
    }

    private fun showContextMenu(v: View) {
        registerForContextMenu(v)
        requireActivity().openContextMenu(v)
        unregisterForContextMenu(v)
    }

    private fun showProgressBar() {
        if (!snackbar.isShown) 
            snackbar.show()
    }

    private fun hideProgressBar() = snackbar.takeIf(Snackbar::isShown)?.run(Snackbar::dismiss)

    private fun showInputMethod() {
        val inputMethodManager = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)

        lifecycleScope.launch {
            delay(500)
            (binding.recyclerView.findViewHolderForAdapterPosition(0) as? WriteListAdapter.WriteViewHolder.HeaderHolder)?.binding?.etText.also {
                inputMethodManager?.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }
}
