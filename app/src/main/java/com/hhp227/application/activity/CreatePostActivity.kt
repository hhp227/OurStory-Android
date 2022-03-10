package com.hhp227.application.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.activity.ImageSelectActivity.Companion.MULTI_SELECT_TYPE
import com.hhp227.application.activity.ImageSelectActivity.Companion.SELECT_TYPE
import com.hhp227.application.adapter.WriteListAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.data.PostRepository
import com.hhp227.application.databinding.ActivityCreatePostBinding
import com.hhp227.application.dto.ListItem
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.viewmodel.CreatePostViewModel
import com.hhp227.application.viewmodel.CreatePostViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CreatePostActivity : AppCompatActivity() {
    private lateinit var snackbar: Snackbar

    private lateinit var binding: ActivityCreatePostBinding

    private val viewModel: CreatePostViewModel by viewModels {
        CreatePostViewModelFactory(PostRepository(), AppController.getInstance().preferenceManager, this, intent.extras)
    }

    private val cameraCaptureImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            viewModel.getBitMap(BitmapUtil(this))?.also {
                viewModel.addItem(
                    ListItem.Image(
                        bitmap = it
                    )
                )
                binding.recyclerView.adapter?.also { adapter -> adapter.notifyItemInserted(adapter.itemCount - 1) }
            }
        }
    }

    private val cameraPickImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getParcelableArrayExtra("data")?.forEach { uri ->
            viewModel.addItem(
                ListItem.Image(
                    bitmap = BitmapUtil(applicationContext).bitmapResize(uri as Uri, 200)
                )
            )
            binding.recyclerView.adapter?.also { adapter -> adapter.notifyItemInserted(adapter.itemCount - 1) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        binding.recyclerView.adapter = WriteListAdapter().apply {
            setOnItemClickListener { v, p ->
                v.setOnCreateContextMenuListener { menu, _, _ ->
                    menu.apply {
                        setHeaderTitle(getString(R.string.select_action))
                        add(0, p, Menu.NONE, getString(R.string.delete))
                    }
                }
                v.showContextMenu()
            }
        }

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.ibImage.setOnClickListener(::showContextMenu)
        binding.ibVideo.setOnClickListener(::showContextMenu)
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> {
                    showProgressBar()
                }
                state.textFormState != null -> {
                    state.textFormState.textError?.let { error ->
                        (binding.recyclerView.findViewHolderForAdapterPosition(0) as? WriteListAdapter.WriteViewHolder.HeaderHolder)?.binding?.etText?.error = getString(error)
                    }
                }
                state.postId >= 0 -> {
                    hideProgressBar()
                    setResult(RESULT_OK)
                    finish()
                }
                state.itemList.isNotEmpty() -> {
                    (binding.recyclerView.adapter as WriteListAdapter).submitList(state.itemList)
                }
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    Snackbar.make(currentFocus!!, state.error, Snackbar.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.write, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        R.id.actionSend -> {
            viewModel.actionSend((binding.recyclerView.adapter as WriteListAdapter).headerHolder.binding.etText.text.trim().toString())
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu?.apply {
            when (v?.id) {
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
            binding.recyclerView.adapter?.notifyItemRemoved(item.itemId)
            true
        }
        1 -> {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_REQUEST)
            true
        }
        2 -> {
            File.createTempFile(
                "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}_", /* prefix */
                ".jpg", /* suffix */
                getExternalFilesDir(Environment.DIRECTORY_PICTURES) /* directory */
            ).also {
                viewModel.currentPhotoPath = it.absolutePath
                viewModel.photoURI = FileProvider.getUriForFile(this, packageName, it)
            }
            cameraCaptureImageActivityResultLauncher.launch(viewModel.photoURI)
            true
        }
        3 -> {
            Toast.makeText(this, "동영상 선택", Toast.LENGTH_LONG).show()
            true
        }
        else -> super.onContextItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST -> if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                Intent(this, ImageSelectActivity::class.java).putExtra(SELECT_TYPE, MULTI_SELECT_TYPE)
                    .also(cameraPickImageActivityResultLauncher::launch)
            }
        }
    }

    private fun showContextMenu(v: View?) {
        registerForContextMenu(v)
        openContextMenu(v)
        unregisterForContextMenu(v)
    }

    private fun showProgressBar() {
        snackbar = Snackbar.make(currentFocus!!, getString(R.string.sending), Snackbar.LENGTH_INDEFINITE).also {
            (it.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup).addView(ProgressBar(applicationContext))
        }

        if (!snackbar.isShown)
            snackbar.show()
    }

    private fun hideProgressBar() = snackbar.takeIf { it.isShown }?.apply { dismiss() }

    companion object {
        private const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045
    }
}