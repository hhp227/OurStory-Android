package com.hhp227.application.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.databinding.FragmentCreateGroupBinding
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.CreateGroupViewModel
import java.io.IOException

class CreateGroupFragment : Fragment(), MenuProvider {
    private var binding: FragmentCreateGroupBinding by autoCleared()

    private val viewModel: CreateGroupViewModel by viewModels {
        InjectorUtils.provideCreateGroupViewModelFactory()
    }

    private val cameraCaptureImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            val bitmap = try {
                viewModel.uri?.let { uri ->
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
        if (result.data != null) {
            val bitmap = BitmapUtil(requireContext()).bitmapResize(result.data?.data, 200)

            viewModel.setBitmap(bitmap)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateGroupBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivGroupImage.setOnClickListener { v ->
            registerForContextMenu(v)
            requireActivity().openContextMenu(v)
            unregisterForContextMenu(v)
        }
        setNavAppBar(binding.toolbar)
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when {
                state.group != null -> {
                    val direction = MainFragmentDirections.actionMainFragmentToGroupDetailFragment(state.group)

                    setFragmentResult(findNavController().previousBackStackEntry?.destination?.displayName ?: "", bundleOf())
                    requireActivity().findNavController(R.id.nav_host).navigateUp()
                    requireActivity().findNavController(R.id.nav_host).navigate(direction)
                    Snackbar.make(requireView(), getString(R.string.group_created), Snackbar.LENGTH_LONG).setAction("Action", null).show()
                }
                state.message.isNotBlank() -> {
                    Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).setAction("Action", null).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(this)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.apply {
            setHeaderTitle(getString(R.string.context_image_title))
            add(getString(R.string.camera))
            add(getString(R.string.gallery))
            add(getString(R.string.non_image))
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.title) {
        getString(R.string.camera) -> {
            cameraCaptureImageActivityResultLauncher.launch(viewModel.getUriToSaveImage())
            true
        }
        getString(R.string.gallery) -> {
            cameraPickImageActivityResultLauncher.launch(Intent(Intent.ACTION_PICK).setType(MediaStore.Images.Media.CONTENT_TYPE).setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
            true
        }
        getString(R.string.non_image) -> {
            viewModel.setBitmap(null)
            true
        }
        else -> super.onContextItemSelected(item)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.group_create, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.actionSend -> {
            viewModel.createGroup(
                viewModel.state.value!!.title,
                viewModel.state.value!!.description,
                viewModel.state.value!!.bitmap,
                if (!viewModel.state.value!!.joinType) "0" else "1"
            )
            true
        }
        else -> false
    }

    private fun setNavAppBar(toolbar: Toolbar) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).addMenuProvider(this)
        toolbar.setupWithNavController(findNavController())
    }
}
