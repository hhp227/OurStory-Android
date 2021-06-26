package com.hhp227.application.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.GalleryItem
import com.hhp227.application.dto.ImageItem
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.volley.util.MultipartRequest
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_write.*
import kotlinx.android.synthetic.main.input_contents.view.*
import kotlinx.android.synthetic.main.input_text.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.properties.Delegates

// TODO WriteActivity 글 작성후 전송되지 않았는데(finish()를 호출하면) 첫화면(헤더와 첫번째 아이템이 보이는 화면)으로 이동함
class WriteActivity : AppCompatActivity() {
    private val mApiKey: String? by lazy { AppController.getInstance().preferenceManager.user.apiKey }

    private val mItemList: MutableList<Any> by lazy { arrayListOf<Any>() }

    private var mImageList: ArrayList<ImageItem>? = null

    private var mPostId by Delegates.notNull<Int>()

    private var mType by Delegates.notNull<Int>()

    private var mGroupId by Delegates.notNull<Int>()

    private lateinit var mText: String

    private lateinit var mCurrentPhotoPath: String

    private lateinit var mHeaderHolder: HeaderHolder

    private lateinit var mPhotoURI: Uri

    private lateinit var mSnackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)
        initialize()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recycler_view.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
                    TYPE_TEXT -> HeaderHolder(LayoutInflater.from(parent.context).inflate(R.layout.input_text, parent, false)).also { mHeaderHolder = it }
                    TYPE_CONTENT -> ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.input_contents, parent, false))
                    else -> throw RuntimeException()
                }

                override fun getItemCount(): Int = mItemList.size

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    if (holder is HeaderHolder) {
                        val text = mItemList[position].toString()

                        holder.containerView.etText.setText(text)
                    } else if (holder is ItemHolder) {
                        (mItemList[position] as ImageItem).let {
                            with(holder) {
                                Glide.with(this@WriteActivity)
                                    .load(when {
                                        it.bitmap != null -> it.bitmap
                                        it.image != null -> URLs.URL_POST_IMAGE_PATH + it.image
                                        else -> null
                                    })
                                    .into(containerView.ivPreview)
                            }
                        }
                    }
                }

                override fun getItemViewType(position: Int): Int = if (mItemList[position] is ImageItem) TYPE_CONTENT else TYPE_TEXT
            }
        }.run {
            mItemList.add(mText)
            mImageList?.let { mItemList.addAll(it) }
            adapter?.notifyDataSetChanged()
        }
        ib_image.setOnClickListener(::showContextMenu)
        ib_video.setOnClickListener(::showContextMenu)
    }

    override fun onDestroy() {
        super.onDestroy()
        mItemList.clear()
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
            val text = mHeaderHolder.containerView.etText.text.toString().trim()

            if (text.isNotEmpty() || mItemList.size > 1) {
                showProgressBar()
                when (mType) {
                    TYPE_INSERT -> actionInsert(text)
                    TYPE_UPDATE -> actionUpdate(text)
                }
            } else
                Snackbar.make(currentFocus!!, "내용을 입력하세요.", Snackbar.LENGTH_LONG).show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu?.apply {
            when (v?.id) {
                R.id.ib_image -> {
                    setHeaderTitle("이미지 선택")
                    add(1, Menu.NONE, Menu.NONE, "갤러리")
                    add(2, Menu.NONE, Menu.NONE, "카메라")
                }
                R.id.ib_video -> {
                    setHeaderTitle("동영상 선택")
                    add(3, Menu.NONE, Menu.NONE, "유튜브")
                }
                else -> super.onCreateContextMenu(menu, v, menuInfo)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.groupId) {
        0 -> {
            mItemList.removeAt(item.itemId)
            recycler_view.adapter?.notifyItemRemoved(item.itemId)
            true
        }
        1 -> {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_REQUEST)
            true
        }
        2 -> {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        null
                    }

                    photoFile?.also {
                        mPhotoURI = FileProvider.getUriForFile(this, packageName, it)

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI)
                        startActivityForResult(takePictureIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE)
                    }
                }
            }
            true
        }
        3 -> {
            Toast.makeText(this, "동영상 선택", Toast.LENGTH_LONG).show()
            true
        }
        else -> super.onContextItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            /*data!!.clipData?.let {
                for (i in 0 until it.itemCount) {
                    ImageItem().apply { bitmap = BitmapUtil(applicationContext).bitmapResize(it.getItemAt(i).uri, 200) }
                        .also { mItemList.add(it) }
                    recyclerView.adapter?.notifyItemInserted(mItemList.size - 1)
                }
            } ?: with(mItemList) {
                add(ImageItem().apply { bitmap = BitmapUtil(applicationContext).bitmapResize(data.data, 200) })
                recyclerView.adapter?.notifyItemInserted(size - 1)
            }*/
            data?.getParcelableArrayExtra("data")?.forEach { uri ->
                mItemList.add(ImageItem(BitmapUtil(applicationContext).bitmapResize(uri as Uri, 200)))
                recycler_view.adapter?.notifyItemInserted(mItemList.size - 1)
            }
        } else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                BitmapUtil(this).bitmapResize(mPhotoURI, 200)?.let {
                    val ei = ExifInterface(mCurrentPhotoPath)
                    val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

                    BitmapUtil(this).rotateImage(it, when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90F
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180F
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270F
                        else -> 0F
                    })
                }.also {
                    mItemList.add(ImageItem(it))
                    recycler_view.adapter?.notifyItemInserted(mItemList.size - 1)
                }
            } catch (e: IOException) {
                Log.e(TAG, e.message!!)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    Intent(this, ImageSelectActivity::class.java).also {
                        startActivityForResult(it, CAMERA_PICK_IMAGE_REQUEST_CODE)
                    }
                }
            }
        }
    }

    private fun initialize() {
        mPostId = intent.getIntExtra("article_id", 0)
        mText = intent.getStringExtra("text")!!
        mType = intent.getIntExtra("type", 0)
        mImageList = intent.getParcelableArrayListExtra("images")
        mGroupId = intent.getIntExtra("group_id", 0)
    }

    private fun uploadImage(position: Int, postId: Int) {
        if ((mItemList[position] as ImageItem).bitmap != null) {
            val multiPartRequest = object : MultipartRequest(Method.POST, URLs.URL_POST_IMAGE, Response.Listener { response ->
                if (!JSONObject(String(response.data)).getBoolean("error"))
                    imageUploadProcess(position, postId)
            }, Response.ErrorListener { error ->
                Snackbar.make(currentFocus!!, "응답에러 ${error.message}", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                hideProgressBar()
            }) {
                override fun getHeaders() = mapOf("Authorization" to mApiKey)

                override fun getByteData() = mapOf(
                    "image" to DataPart("${System.currentTimeMillis()}.jpg", ByteArrayOutputStream().also {
                        (mItemList[position] as ImageItem).bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
                    }.toByteArray())
                )

                override fun getParams() = mapOf("post_id" to postId.toString())
            }

            AppController.getInstance().addToRequestQueue(multiPartRequest)
        } else
            imageUploadProcess(position, postId, 0L)
    }

    private fun imageUploadProcess(position: Int, postId: Int, millis: Long = 700L) {
        var count = position

        try {
            if (count < mItemList.size - 1) {
                count++
                Thread.sleep(millis)
                uploadImage(count, postId)
            } else {
                hideProgressBar()
                when (mType) {
                    TYPE_INSERT -> {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    TYPE_UPDATE -> {
                        setResult(Activity.RESULT_OK, Intent(this, PostDetailActivity::class.java))
                        finish()
                    }
                }
            }
        } catch (e: Exception) {
            Snackbar.make(currentFocus!!, "이미지 업로드 실패", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            hideProgressBar()
        }
    }
    private fun deleteImages(postId: Int) {
        val tagStringReq = "req_delete_image"
        val imageIdJsonArray = JSONArray().apply {
            mImageList?.forEach { i ->
                if (mItemList.indexOf(i) == -1)
                    this.put(i.id)
            }
        }
        val stringRequest = object : StringRequest(Method.POST, URLs.URL_POST_IMAGE_DELETE, Response.Listener { response ->
            Log.e(TAG, response)
        }, Response.ErrorListener { error ->
            error.message?.let {
                Log.e(TAG, it)
                Snackbar.make(currentFocus!!, it, Snackbar.LENGTH_LONG).show()
            }
        }) {
            override fun getHeaders() = mapOf("Authorization" to mApiKey)

            override fun getParams() = mapOf("ids" to imageIdJsonArray.toString(), "post_id" to postId.toString())
        }

        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
    }

    private fun actionInsert(text: String) {
        val tagStringReq = "req_insert"
        val stringRequest = object : StringRequest(Method.POST, URLs.URL_POST, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {
                    val postId = jsonObject.getInt("post_id")

                    if (mItemList.size > 1) {
                        val position = 1

                        uploadImage(position, postId)
                    } else {
                        hideProgressBar()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                } else
                    Snackbar.make(currentFocus!!, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show()
            } catch (e: JSONException) {
                Log.e(TAG, e.message!!)
            }
        }, Response.ErrorListener { error ->
            if (error.cause is UnknownHostException)
                Snackbar.make(currentFocus!!, "네트워크 연결을 확인해주세요.", Snackbar.LENGTH_LONG).show()
            error.message?.let {
                Log.e(TAG, it)
                Snackbar.make(currentFocus!!, it, Snackbar.LENGTH_LONG).show()
            }
            hideProgressBar()
        }) {
            override fun getHeaders() = mapOf("Authorization" to mApiKey)

            override fun getParams() = mapOf("text" to text, "group_id" to mGroupId.toString())
        }

        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
    }

    private fun actionUpdate(text: String) {
        val tagStringReq = "req_update"
        val stringRequest = object : StringRequest(Method.PUT, "${URLs.URL_POST}/$mPostId", Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {

                    // 이미지 삭제 체크
                    deleteImages(mPostId)
                    if (mItemList.size > 1) {
                        val position = 1

                        uploadImage(position, mPostId)
                    } else {
                        hideProgressBar()
                        setResult(Activity.RESULT_OK, Intent(this, PostDetailActivity::class.java))
                        finish()
                    }
                } else
                    Snackbar.make(currentFocus!!, "에러 $response", Snackbar.LENGTH_LONG).show()
            } catch (e: JSONException) {
                Log.e(TAG, e.message!!)
            }
        }, Response.ErrorListener { error ->
            error.message?.let {
                Log.e(TAG, it)
                Snackbar.make(currentFocus!!, it, Snackbar.LENGTH_LONG).show()
            }
            hideProgressBar()
        }) {
            override fun getHeaders() = mapOf("Authorization" to mApiKey)

            override fun getParams() = mapOf("text" to text, "status" to "0")
        }

        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply { mCurrentPhotoPath = absolutePath }
    }

    private fun showContextMenu(v: View?) {
        registerForContextMenu(v)
        openContextMenu(v)
        unregisterForContextMenu(v)
    }

    private fun showProgressBar() {
        Snackbar.make(currentFocus!!, "전송중...", Snackbar.LENGTH_INDEFINITE).let {
            mSnackbar = it

            it.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup
        }.also {
            it.addView(ProgressBar(applicationContext))
        }
        if (!mSnackbar.isShown)
            mSnackbar.show()
    }

    private fun hideProgressBar() = mSnackbar.takeIf { it.isShown }?.apply { dismiss() }

    inner class HeaderHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer

    inner class ItemHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        init {
            containerView.setOnClickListener { v ->
                v.setOnCreateContextMenuListener { menu, _, _ ->
                    menu.apply {
                        setHeaderTitle("작업 선택")
                        add(0, adapterPosition, Menu.NONE, "삭제")
                    }
                }
                v.showContextMenu()
            }
        }
    }

    companion object {
        const val TYPE_INSERT = 0
        const val TYPE_UPDATE = 1
        const val CAMERA_PICK_IMAGE_REQUEST_CODE = 10
        const val CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 20
        private val TAG = WriteActivity::class.java.simpleName
        private const val TYPE_TEXT = 100
        private const val TYPE_CONTENT = 200
        private const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045
    }
}