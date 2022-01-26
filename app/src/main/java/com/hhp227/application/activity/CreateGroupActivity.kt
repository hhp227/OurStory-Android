package com.hhp227.application.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.activity.WriteActivity.Companion.CAMERA_CAPTURE_IMAGE_REQUEST_CODE
import com.hhp227.application.activity.WriteActivity.Companion.CAMERA_PICK_IMAGE_REQUEST_CODE
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.ActivityCreateGroupBinding
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.viewmodel.CreateGroupViewModel
import com.hhp227.application.volley.util.MultipartRequest
import kotlinx.coroutines.flow.onEach
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CreateGroupActivity : AppCompatActivity() {
    private val viewModel: CreateGroupViewModel by viewModels()

    private val cameraCaptureImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            viewModel.setBitmap(BitmapUtil(this))
            binding.ivGroupImage.setImageBitmap(viewModel.bitMap)
        }
    }

    private val cameraPickImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.data != null) {
            viewModel.bitMap = BitmapUtil(this).bitmapResize(result.data?.data, 200)

            binding.ivGroupImage.setImageBitmap(viewModel.bitMap)
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
            }
        }
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
            /*if (title.isNotEmpty() && description.isNotEmpty()) {
                viewModel.bitMap?.let { groupImageUpload(title, description, joinType) } ?: createGroup(title, null, description, joinType)
            } else {
                binding.etTitle.error = if (title.isEmpty()) getString(R.string.require_group_title) else null

                if (description.isEmpty())
                    Snackbar.make(currentFocus!!, getString(R.string.require_group_description), Snackbar.LENGTH_LONG).setAction("Action", null).show()
            }*/
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
            viewModel.bitMap = null

            binding.ivGroupImage.setImageResource(R.drawable.add_photo)
            Snackbar.make(currentFocus!!, "기본 이미지 선택", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            true
        }
        else -> super.onContextItemSelected(item)
    }

    /*private fun createGroup(title: String, image: String?, description: String, joinType: String) {
        val stringRequest = object : StringRequest(Method.POST, URLs.URL_GROUP,  Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {
                    val intent = Intent(this, GroupActivity::class.java).apply {
                        putExtra("group_id", jsonObject.getInt("group_id"))
                        putExtra("group_name", jsonObject.getString("group_name"))
                    }

                    setResult(Activity.RESULT_OK)
                    startActivity(intent)
                    finish()
                    Snackbar.make(currentFocus!!, getString(R.string.group_created), Snackbar.LENGTH_LONG).setAction("Action", null).show()
                }
            } catch (e: JSONException) {

            }
        }, Response.ErrorListener { error ->
            error.message?.let {
                VolleyLog.e(TAG, it)
            }
        }) {
            override fun getHeaders() = mapOf("Authorization" to viewModel.apiKey)

            override fun getParams() = mapOf(
                "name" to title,
                "description" to description,
                "join_type" to joinType,
                "image" to image
            )
        }

        AppController.getInstance().addToRequestQueue(stringRequest)
    }*/

    /*private fun groupImageUpload(title: String, description: String, joinType: String) {
        val multipartRequest = object : MultipartRequest(Method.POST, URLs.URL_GROUP_IMAGE, Response.Listener { response ->
            val jsonObject = JSONObject(String(response.data))
            val image = jsonObject.getString("image")

            if (!jsonObject.getBoolean("error"))
                createGroup(title, image, description, joinType)
        }, Response.ErrorListener { error ->
            error.message?.let {
                VolleyLog.e(TAG, it)
            }
        }) {
            override fun getHeaders() = mapOf("Authorization" to viewModel.apiKey)

            override fun getByteData() = mapOf(
                "image" to DataPart("${System.currentTimeMillis()}.jpg", ByteArrayOutputStream().also {
                    viewModel.bitMap?.compress(Bitmap.CompressFormat.PNG, 80, it)
                }.toByteArray())
            )
        }

        AppController.getInstance().addToRequestQueue(multipartRequest)
    }*/

    companion object {
        private val TAG = CreateGroupActivity::class.java.simpleName
    }
}
