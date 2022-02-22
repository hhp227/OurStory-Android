package com.hhp227.application.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.databinding.ActivityCreateGroupBinding
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.viewmodel.CreateGroupViewModel
import com.hhp227.application.viewmodel.CreateGroupViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CreateGroupActivity : AppCompatActivity() {
    private val viewModel: CreateGroupViewModel by viewModels {
        CreateGroupViewModelFactory(GroupRepository())
    }

    private val cameraCaptureImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            viewModel.setBitmap(BitmapUtil(this))
            binding.ivGroupImage.setImageBitmap(viewModel.bitmap)
        }
    }

    private val cameraPickImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.data != null) {
            viewModel.bitmap = BitmapUtil(this).bitmapResize(result.data?.data, 200)

            binding.ivGroupImage.setImageBitmap(viewModel.bitmap)
        }
    }

    private lateinit var binding: ActivityCreateGroupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.etTitle.doOnTextChanged { text, _, _, _ ->
            binding.ivReset.setImageResource(if (text!!.isNotEmpty()) R.drawable.ic_clear_black_24dp else R.drawable.ic_clear_gray_24dp )
        }
        binding.ivReset.setOnClickListener { binding.etTitle.setText("") }
        binding.ivGroupImage.setOnClickListener { v ->
            registerForContextMenu(v)
            openContextMenu(v)
            unregisterForContextMenu(v)
        }
        binding.rgJoinType.apply {
            check(R.id.rb_auto)
            setOnCheckedChangeListener { _, checkedId -> viewModel.joinType = checkedId != R.id.rb_auto }
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> {
                    // TODO
                }
                state.createGroupFormState != null -> {
                    state.createGroupFormState.titleError?.let { error -> binding.etTitle.error = getString(error) }
                    state.createGroupFormState.descError?.let { error -> Snackbar.make(currentFocus!!, getString(error), Snackbar.LENGTH_LONG).setAction("Action", null).show() }
                }
                state.group != null -> {
                    val intent = Intent(this, GroupActivity::class.java)
                        .putExtra("group", state.group)

                    setResult(Activity.RESULT_OK)
                    startActivity(intent)
                    finish()
                    Snackbar.make(currentFocus!!, getString(R.string.group_created), Snackbar.LENGTH_LONG).setAction("Action", null).show()
                }
                state.error.isNotBlank() -> {
                    Snackbar.make(currentFocus!!, state.error, Snackbar.LENGTH_LONG).setAction("Action", null).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.group_create, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        R.id.actionSend -> {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val joinType = if (!viewModel.joinType) "0" else "1"

            viewModel.createGroup(title, description, joinType)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.apply {
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
                getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ).also { file ->
                viewModel.uri = FileProvider.getUriForFile(this, applicationContext.packageName, file)
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
            viewModel.bitmap = null

            binding.ivGroupImage.setImageResource(R.drawable.add_photo)
            Snackbar.make(currentFocus!!, "기본 이미지 선택", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            true
        }
        else -> super.onContextItemSelected(item)
    }
}
