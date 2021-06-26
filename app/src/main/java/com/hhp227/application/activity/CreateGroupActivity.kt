package com.hhp227.application.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.activity.WriteActivity.Companion.CAMERA_CAPTURE_IMAGE_REQUEST_CODE
import com.hhp227.application.activity.WriteActivity.Companion.CAMERA_PICK_IMAGE_REQUEST_CODE
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.volley.util.MultipartRequest
import kotlinx.android.synthetic.main.activity_create_group.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class CreateGroupActivity : AppCompatActivity() {
    private val mApiKey: String? by lazy { AppController.getInstance().preferenceManager.user.apiKey }

    private val mTextWatcher: TextWatcher by lazy {
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                iv_reset.setImageResource(if (s!!.isNotEmpty()) R.drawable.ic_clear_black_24dp else R.drawable.ic_clear_gray_24dp )
            }
        }
    }

    private var mBitMap: Bitmap? = null

    private var mJoinType = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        et_title.addTextChangedListener(mTextWatcher)
        iv_reset.setOnClickListener { et_title.setText("") }
        iv_group_image.setOnClickListener { v ->
            registerForContextMenu(v)
            openContextMenu(v)
            unregisterForContextMenu(v)
        }
        rg_join_type.apply {
            check(R.id.rb_auto)
            setOnCheckedChangeListener { _, checkedId -> mJoinType = checkedId != R.id.rb_auto }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        et_title.removeTextChangedListener(mTextWatcher)
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
            val title = et_title.text.toString().trim()
            val description = et_description.text.toString().trim()
            val joinType = if (!mJoinType) "0" else "1"

            if (title.isNotEmpty() && description.isNotEmpty()) {
                mBitMap?.let { groupImageUpload(title, description, joinType) } ?: createGroup(title, null, description, joinType)
            } else {
                et_title.error = if(title.isEmpty()) "그룹명을 입력하세요." else null

                if (description.isEmpty())
                    Snackbar.make(currentFocus!!, "그룹설명을 입력해주세요.", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            }
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
            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_CAPTURE_IMAGE_REQUEST_CODE)
            true
        }
        getString(R.string.gallery) -> {
            startActivityForResult(Intent(Intent.ACTION_PICK).setType(MediaStore.Images.Media.CONTENT_TYPE).setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI), CAMERA_PICK_IMAGE_REQUEST_CODE)
            true
        }
        getString(R.string.non_image) -> {
            mBitMap = null

            iv_group_image.setImageResource(R.drawable.add_photo)
            Snackbar.make(currentFocus!!, "기본 이미지 선택", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            true
        }
        else -> super.onContextItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            mBitMap = data.extras!!.get("data") as Bitmap?

            iv_group_image.setImageBitmap(mBitMap)
        } else if (requestCode == CAMERA_PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            mBitMap = BitmapUtil(this).bitmapResize(data.data, 200)

            iv_group_image.setImageBitmap(mBitMap)
        }
    }

    private fun createGroup(title: String, image: String?, description: String, joinType: String) {
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
                    Snackbar.make(currentFocus!!, "그룹이 생성되었습니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                }
            } catch (e: JSONException) {

            }
        }, Response.ErrorListener { error ->
            error.message?.let {
                VolleyLog.e(TAG, it)
            }
        }) {
            override fun getHeaders() = mapOf("Authorization" to mApiKey)

            override fun getParams() = mapOf(
                "name" to title,
                "description" to description,
                "join_type" to joinType,
                "image" to image
            )
        }

        AppController.getInstance().addToRequestQueue(stringRequest)
    }

    private fun groupImageUpload(title: String, description: String, joinType: String) {
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
            override fun getHeaders() = mapOf("Authorization" to mApiKey)

            override fun getByteData() = mapOf(
                "image" to DataPart("${System.currentTimeMillis()}.jpg", ByteArrayOutputStream().also {
                    mBitMap?.compress(Bitmap.CompressFormat.PNG, 80, it)
                }.toByteArray())
            )
        }

        AppController.getInstance().addToRequestQueue(multipartRequest)
    }

    companion object {
        private val TAG = CreateGroupActivity::class.java.simpleName
    }
}
