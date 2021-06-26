package com.hhp227.application.activity

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.Tab1Fragment
import com.hhp227.application.Tab1Fragment.FEEDINFO_CODE
import com.hhp227.application.activity.WriteActivity.Companion.TYPE_UPDATE
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.*
import com.hhp227.application.util.Utils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.post_detail.tv_text
import kotlinx.android.synthetic.main.post_detail.view.*
import kotlinx.android.synthetic.main.item_reply.view.iv_profile_image
import kotlinx.android.synthetic.main.item_reply.view.tv_create_at
import kotlinx.android.synthetic.main.item_reply.view.tv_reply
import kotlinx.android.synthetic.main.item_reply.view.tv_name
import org.json.JSONException
import org.json.JSONObject
import kotlin.properties.Delegates

class PostDetailActivity : AppCompatActivity() {
    private val mItemList: MutableList<Any> by lazy { arrayListOf() }

    private val mUser: User by lazy { AppController.getInstance().preferenceManager.user }

    private var mMyUserId by Delegates.notNull<Int>()

    private var mUserId by Delegates.notNull<Int>()

    private var mPostId by Delegates.notNull<Int>()

    private var mPosition by Delegates.notNull<Int>()

    private var mGroupId by Delegates.notNull<Int>()

    private var mGroupName: String? = null

    private var mIsBottom by Delegates.notNull<Boolean>()

    private var mIsUpdate by Delegates.notNull<Boolean>()

    private lateinit var mTextWatcher: TextWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        initialize()
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = if (TextUtils.isEmpty(mGroupName)) getString(R.string.main_fragment) else mGroupName

            setDisplayHomeAsUpEnabled(true)
        }
        srl_post.setOnRefreshListener {
            Handler().postDelayed({
                srl_post.isRefreshing = false

                mItemList.clear()
                fetchArticleData()
            }, 1000)
        }
        cv_btn_send.setOnClickListener { v ->
            val text = et_reply.text.toString().trim()

            if (text.isNotEmpty()) {
                val tagStringReq = "req_send"
                val stringRequest = object : StringRequest(Method.POST, URLs.URL_REPLYS.replace("{POST_ID}", mPostId.toString()), Response.Listener { response ->
                    hideProgressBar()
                    try {
                        val jsonObject = JSONObject(response)

                        if (!jsonObject.getBoolean("error")) {
                            Toast.makeText(applicationContext, "전송 완료", Toast.LENGTH_LONG).show()
                            mItemList.clear()
                            fetchArticleData()

                            // 전송할때마다 하단으로
                            moveToBottom()
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, e.message!!)
                    }
                }, Response.ErrorListener { error ->
                    error.message?.let {
                        Log.e(TAG, it)
                        Toast.makeText(applicationContext, it, Toast.LENGTH_LONG).show()
                    }
                    hideProgressBar()
                }) {
                    override fun getHeaders() = mapOf("Authorization" to mUser.apiKey)

                    override fun getParams() = mapOf("reply" to text)
                }

                AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
                et_reply.setText("")
                v?.let { (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(v.windowToken, 0) }
            } else
                Toast.makeText(applicationContext, "메시지를 입력하세요.", Toast.LENGTH_LONG).show()
        }
        et_reply.addTextChangedListener(mTextWatcher)
        rv_post.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
                    TYPE_ARTICLE -> HeaderHolder(LayoutInflater.from(context).inflate(R.layout.post_detail, parent, false))
                    TYPE_REPLY -> ItemHolder(LayoutInflater.from(context).inflate(R.layout.item_reply, parent, false))
                    else -> throw RuntimeException()
                }

                override fun getItemCount(): Int = mItemList.size

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    if (holder is HeaderHolder) {
                        holder.bind(mItemList[position] as PostItem)
                    } else if (holder is ItemHolder) {
                        holder.bind(mItemList[position] as ReplyItem)
                    }
                }

                override fun getItemViewType(position: Int): Int = if (mItemList[position] is ReplyItem) TYPE_REPLY else TYPE_ARTICLE
            }
        }
        fetchArticleData()
    }

    override fun onDestroy() {
        super.onDestroy()
        et_reply.removeTextChangedListener(mTextWatcher)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FEEDINFO_CODE && resultCode == Activity.RESULT_OK) {
            mIsUpdate = true

            mItemList.clear()
            fetchArticleData()
        } else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val position = data!!.getIntExtra("position", 0)

            (mItemList[position] as ReplyItem).run {
                reply = data.getStringExtra("reply")
                mItemList[position] = this

                rv_post.adapter?.notifyItemChanged(position)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {

            // 조건을 위해 xml레이아웃을 사용하지 않고 코드로 옵션메뉴를 구성함
            if (mUserId == mMyUserId) {
                add(Menu.NONE, 1, Menu.NONE, getString(R.string.modify))
                add(Menu.NONE, 2, Menu.NONE, R.string.remove)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        1 -> {
            val intent = Intent(this, WriteActivity::class.java).apply {
                putExtra("type", TYPE_UPDATE)
                putExtra("article_id", mPostId)
                putExtra("text", tv_text.text.toString().trim())
                putParcelableArrayListExtra("images", (mItemList[0] as PostItem).imageItemList as java.util.ArrayList<out Parcelable>)
            }

            startActivityForResult(intent, FEEDINFO_CODE)
            true
        }
        2 -> {
            val tagStringReq = "req_delete"
            val stringRequest = object : StringRequest(Method.DELETE, "${URLs.URL_POST}/$mPostId", Response.Listener { response ->
                hideProgressBar()
                try {
                    val jsonObject = JSONObject(response)

                    if (!jsonObject.getBoolean("error")) {
                        setResult(Activity.RESULT_OK)
                        finish()
                        Toast.makeText(applicationContext, "삭제 완료", Toast.LENGTH_LONG).show()
                    } else
                        Toast.makeText(applicationContext, "삭제할 수 없습니다.", Toast.LENGTH_LONG).show()
                } catch (e: JSONException) {
                    Log.e(TAG, e.message!!)
                }
            }, Response.ErrorListener { error ->
                error.message?.let {
                    Log.e(TAG, "전송 에러 : $it")
                    Toast.makeText(applicationContext, it, Toast.LENGTH_LONG).show()
                }
                hideProgressBar()
            }) {
                override fun getHeaders() = mapOf("Authorization" to mUser.apiKey)
            }

            showProgressBar()
            AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.groupId) {
        0 -> {
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).text = if (mItemList[item.itemId] is PostItem) (mItemList[item.itemId] as PostItem).text else (mItemList[item.itemId] as ReplyItem).reply
            Toast.makeText(applicationContext, "클립보드에 복사되었습니다!", Toast.LENGTH_LONG).show()
            true
        }
        1 -> {
            val intent = Intent(this, ReplyModifyActivity::class.java).apply {
                (mItemList[item.itemId] as ReplyItem).let {
                    putExtra("reply_id", it.id)
                    putExtra("text", it.reply)
                    putExtra("position", item.itemId)
                }
            }

            startActivityForResult(intent, REQUEST_CODE)
            true
        }
        2 -> {
            val replyId = (mItemList[item.itemId] as ReplyItem).id
            val tagStringReq = "req_delete"
            val stringRequest = object : StringRequest(Method.DELETE, URLs.URL_REPLY.replace("{REPLY_ID}", replyId.toString()), Response.Listener { response ->
                hideProgressBar()
                try {
                    val jsonObject = JSONObject(response)

                    if (!jsonObject.getBoolean("error")) {
                        mItemList.removeAt(item.itemId)
                        rv_post.adapter?.notifyItemRemoved(item.itemId)
                        Toast.makeText(applicationContext, "삭제 완료", Toast.LENGTH_LONG).show()
                    } else
                        Toast.makeText(applicationContext, "삭제할수 없습니다.", Toast.LENGTH_LONG).show()
                } catch (e: JSONException) {
                    Log.e(TAG, e.message!!)
                }
            }, Response.ErrorListener { error ->
                error.message?.let {
                    Log.e(TAG, it)
                    Toast.makeText(applicationContext, "전송 에러: $it", Toast.LENGTH_LONG).show()
                }
                hideProgressBar()
            }) {
                override fun getHeaders() = hashMapOf("Authorization" to mUser.apiKey)
            }

            showProgressBar()
            AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
            true
        }
        else -> super.onContextItemSelected(item)
    }

    private fun initialize() {
        mMyUserId = mUser.id
        mUserId = intent.getIntExtra("user_id", 0)
        mPostId = intent.getIntExtra("post_id", 0)
        mIsBottom = intent.getBooleanExtra("is_bottom", false)
        mPosition = intent.getIntExtra("position", 0)
        mGroupId = intent.getIntExtra("group_id", 0)
        mGroupName = intent.getStringExtra("group_name")
        mIsUpdate = false
        mTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                cv_btn_send.setCardBackgroundColor(ContextCompat.getColor(applicationContext, if (s!!.isNotEmpty()) R.color.colorAccent else R.color.cardview_light_background))
                tv_btn_send.setTextColor(ContextCompat.getColor(applicationContext, if (s.isNotEmpty()) android.R.color.white else android.R.color.darker_gray))
            }
        }
    }

    private fun fetchArticleData() {
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, "${URLs.URL_POST}/$mPostId", null,  Response.Listener { response ->
            hideProgressBar()
            try {
                PostItem().run {
                    with(response) {
                        id = getInt("id")
                        userId = getInt("user_id")
                        name = getString("name")
                        text = getString("text")
                        profileImage = getString("profile_img")
                        timeStamp = getString("created_at")
                        replyCount = getInt("reply_count")
                        likeCount = getInt("like_count")
                        imageItemList = getJSONObject("attachment").getJSONArray("images").let {
                            val imageList = ArrayList<ImageItem>()

                            for (i in 0 until it.length()) {
                                ImageItem().run {
                                    with(it.getJSONObject(i)) {
                                        id = getInt("id")
                                        image = getString("image")
                                        tag = getString("tag")
                                    }
                                    imageList.add(this)
                                }
                            }
                            imageList
                        }
                    }
                    mItemList.add(0, this)
                    if (mIsUpdate)
                        deliveryUpdate(this)
                }
                rv_post.adapter?.notifyDataSetChanged()
                fetchReplyData()
            } catch (e: JSONException) {
                Toast.makeText(applicationContext, "에러: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, Response.ErrorListener { error ->
            error.message?.let {
                VolleyLog.e(TAG, it)
            }
            hideProgressBar()
        }) {

        }

        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
    }

    private fun fetchReplyData() {
        val jsonArrayRequest = object : JsonArrayRequest(Method.GET, URLs.URL_REPLYS.replace("{POST_ID}", mPostId.toString()), null, Response.Listener { response ->
            hideProgressBar()
            try {
                for (i in 0 until response.length()) {
                    ReplyItem().run {
                        with(response.getJSONObject(i)) {
                            id = getInt("id")
                            userId = getInt("user_id")
                            name = getString("name")
                            reply = getString("reply")
                            profileImage = getString("profile_img")
                            timeStamp = getString("created_at")
                        }
                        mItemList.add(this)
                    }
                    rv_post.adapter?.notifyItemChanged(i + 1)
                }
                if (mIsBottom)
                    moveToBottom()
            } catch (e: JSONException) {
                Log.e(TAG, e.message!!)
            }
        }, Response.ErrorListener { error ->
            error.message?.let {
                VolleyLog.e(TAG, it)
            }
            hideProgressBar()
        }) {
            override fun getHeaders() = mapOf("Authorization" to mUser.apiKey)
        }

        AppController.getInstance().addToRequestQueue(jsonArrayRequest)
    }

    private fun moveToBottom() {
        Handler().postDelayed({
            mIsBottom = false

            rv_post.scrollToPosition(mItemList.size - 1)
        }, 300)
    }

    private fun deliveryUpdate(postItem: PostItem) {
        val intent = Intent(this, Tab1Fragment::class.java).apply {
            with(postItem) {
                putExtra("article_id", id)
                putExtra("text", text)
                putParcelableArrayListExtra("images", imageItemList as java.util.ArrayList<out Parcelable>)
                putExtra("reply_count", replyCount)
                putExtra("position", mPosition)
            }
        }

        setResult(FEEDINFO_CODE, intent)
    }

    private fun showProgressBar() = progress_bar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = progress_bar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    inner class HeaderHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        init {
            containerView.setOnLongClickListener { v ->
                v.apply {
                    setOnCreateContextMenuListener { menu, _, _ ->
                        menu.apply {
                            setHeaderTitle("작업 선택")
                            add(0, adapterPosition, Menu.NONE, "내용 복사")
                        }
                    }
                    showContextMenu()
                }
                true
            }
        }

        fun bind(postItem: PostItem) {
            with(containerView) {
                tv_name.text = postItem.name
                tv_create_at.text = Utils.getPeriodTimeGenerator(context as Activity?, postItem.timeStamp)

                if (!TextUtils.isEmpty(postItem.text)) {
                    tv_text.text = postItem.text
                    tv_text.visibility = View.VISIBLE
                } else
                    tv_text.visibility = View.GONE
                if (postItem.imageItemList.isNotEmpty()) {
                    ll_image.visibility = View.VISIBLE

                    ll_image.removeAllViews()
                    postItem.imageItemList.forEach { imageItem ->
                        ImageView(context).apply {
                            adjustViewBounds = true
                            scaleType = ImageView.ScaleType.FIT_XY

                            setPadding(0, 0, 0, 30)
                            Glide.with(context)
                                .load("${URLs.URL_POST_IMAGE_PATH}${imageItem.image}")
                                .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                                .into(this)
                        }.also { ll_image.addView(it) } // apply().also() -> run()으로 바꿀수 있음
                    }
                } else
                    ll_image.visibility = View.GONE
                tv_like_count.text = postItem.likeCount.toString()
                tv_reply_count.text = postItem.replyCount.toString()

                Glide.with(context)
                    .load("${URLs.URL_USER_PROFILE_IMAGE}${postItem.profileImage}")
                    .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                    .into(iv_profile_image)
            }
        }
    }

    inner class ItemHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        init {
            containerView.setOnLongClickListener { v ->
                v.apply {
                    setOnCreateContextMenuListener { menu, _, _ ->
                        menu.apply {
                            setHeaderTitle("작업 선택")
                            add(0, adapterPosition, Menu.NONE, "내용 복사")
                            if ((mItemList[adapterPosition] as ReplyItem).userId == mMyUserId) {
                                add(1, adapterPosition, Menu.NONE, "댓글 수정")
                                add(2, adapterPosition, Menu.NONE, "댓글 삭제")
                            }
                        }
                    }
                    showContextMenu()
                }
                true
            }
        }

        fun bind(replyItem: ReplyItem) {
            with(containerView) {
                tv_name.text = replyItem.name
                tv_reply.text = replyItem.reply
                tv_create_at.text = Utils.getPeriodTimeGenerator(context as Activity?, replyItem.timeStamp)

                Glide.with(context)
                    .load("${URLs.URL_USER_PROFILE_IMAGE}${(replyItem.profileImage)}")
                    .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                    .into(iv_profile_image)
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 0
        private const val TYPE_ARTICLE = 10
        private const val TYPE_REPLY = 20
        private val TAG = PostDetailActivity::class.simpleName
    }
}