package com.hhp227.application.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.databinding.FragmentCreateGroupBinding
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.viewmodel.CreateGroupViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// TODO state 로직 변경
class CreateGroupFragment : Fragment() {
    private lateinit var binding: FragmentCreateGroupBinding

    private val viewModel: CreateGroupViewModel by viewModels {
        InjectorUtils.provideCreateGroupViewModelFactory()
    }

    private val cameraCaptureImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            val bitmap = try {
                BitmapUtil(requireContext()).bitmapResize(viewModel.uri, 200)?.let {
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.apply {
            setupWithNavController(findNavController())
            inflateMenu(R.menu.group_create)
            setOnMenuItemClickListener(::onOptionsItemSelected)
        }
        binding.etTitle.doOnTextChanged { text, _, _, _ ->
            binding.ivReset.setImageResource(if (!TextUtils.isEmpty(text)) R.drawable.ic_clear_black_24dp else R.drawable.ic_clear_gray_24dp )
        }
        binding.ivReset.setOnClickListener { binding.etTitle.setText("") }
        binding.ivGroupImage.setOnClickListener { v ->
            registerForContextMenu(v)
            requireActivity().openContextMenu(v)
            unregisterForContextMenu(v)
        }
        binding.rgJoinType.apply {
            check(R.id.rb_auto)
            setOnCheckedChangeListener { _, checkedId -> viewModel.joinType = checkedId != R.id.rb_auto }
        }
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                when {
                    state.isLoading -> {
                        // TODO
                    }
                    state.group != null -> {
                        val direction = MainFragmentDirections.actionMainFragmentToGroupDetailFragment(state.group)

                        setFragmentResult("result1", bundleOf())
                        requireActivity().findNavController(R.id.nav_host).navigateUp()
                        requireActivity().findNavController(R.id.nav_host).navigate(direction)
                        Snackbar.make(requireView(), getString(R.string.group_created), Snackbar.LENGTH_LONG).setAction("Action", null).show()
                    }
                    state.error.isNotBlank() -> {
                        Snackbar.make(requireView(), state.error, Snackbar.LENGTH_LONG).setAction("Action", null).show()
                    }
                }
            }
            .launchIn(lifecycleScope)
        viewModel.createGroupFormState
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                state.titleError?.let { error -> binding.etTitle.error = getString(error) }
                state.descError?.let { error -> Snackbar.make(requireView(), getString(error), Snackbar.LENGTH_LONG).setAction("Action", null).show() }
            }
            .launchIn(lifecycleScope)
        viewModel.bitmapFlow
            .onEach { bitmap ->
                binding.ivGroupImage.setImageBitmap(bitmap ?: ResourcesCompat.getDrawable(resources, R.drawable.add_photo, null)?.toBitmap())
            }
            .launchIn(lifecycleScope)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.actionSend -> {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val joinType = if (!viewModel.joinType) "0" else "1"

            viewModel.createGroup(title, description, joinType)
            true
        }
        else -> super.onOptionsItemSelected(item)
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
            File.createTempFile(
                "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}_", /* prefix */
                ".jpg", /* suffix */
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ).also { file ->
                viewModel.uri = FileProvider.getUriForFile(requireContext(), requireContext().packageName, file)
                viewModel.currentPhotoPath = file.absolutePath
            }
            cameraCaptureImageActivityResultLauncher.launch(viewModel.uri)
            true
        }
        getString(R.string.gallery) -> {
            cameraPickImageActivityResultLauncher.launch(Intent(Intent.ACTION_PICK).setType(MediaStore.Images.Media.CONTENT_TYPE).setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
            true
        }
        getString(R.string.non_image) -> {
            viewModel.setBitmap(null)
            Snackbar.make(requireView(), "기본 이미지 선택", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            true
        }
        else -> super.onContextItemSelected(item)
    }
}
