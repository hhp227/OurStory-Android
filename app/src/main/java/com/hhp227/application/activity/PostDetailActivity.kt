package com.hhp227.application.activity

import Tab1Fragment
import Tab1Fragment.Companion.POST_INFO_CODE
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.activity.WriteActivity.Companion.TYPE_UPDATE
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.ActivityPostBinding
import com.hhp227.application.databinding.ItemReplyBinding
import com.hhp227.application.databinding.PostDetailBinding
import com.hhp227.application.dto.*
import com.hhp227.application.util.Utils
import com.hhp227.application.viewmodel.PostDetailViewModel
import org.json.JSONException
import org.json.JSONObject

class PostDetailActivity : AppCompatActivity() {
    private val viewModel: PostDetailViewModel by viewModels()

    private lateinit var headerHolder: HeaderHolder

    private lateinit var binding: ActivityPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)

        setContentView(binding.root)
        initialize()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.run {
            title = if (TextUtils.isEmpty(viewModel.groupName)) getString(R.string.main_fragment) else viewModel.groupName

            setDisplayHomeAsUpEnabled(true)
        }
        binding.srlPost.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.srlPost.isRefreshing = false

                viewModel.itemList.clear()
                fetchArticleData()
            }, 1000)
        }
        binding.cvBtnSend.setOnClickListener { v ->
            val text = binding.etReply.text.toString().trim()

            if (text.isNotEmpty()) {
                val tagStringReq = "req_send"
                val stringRequest = object : StringRequest(Method.POST, URLs.URL_REPLYS.replace("{POST_ID}", viewModel.post.id.toString()), Response.Listener { response ->
                    hideProgressBar()
                    try {
                        val jsonObject = JSONObject(response)

                        if (!jsonObject.getBoolean("error")) {
                            Toast.makeText(applicationContext, "전송 완료", Toast.LENGTH_LONG).show()
                            viewModel.itemList.clear()
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
                    override fun getHeaders() = mapOf("Authorization" to viewModel.user.apiKey)

                    override fun getParams() = mapOf("reply" to text)
                }

                AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
                binding.etReply.setText("")
                v?.let { (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(v.windowToken, 0) }
            } else
                Toast.makeText(applicationContext, "메시지를 입력하세요.", Toast.LENGTH_LONG).show()
        }
        binding.etReply.doOnTextChanged { text, _, _, _ ->
            binding.cvBtnSend.setCardBackgroundColor(ContextCompat.getColor(applicationContext, if (text!!.isNotEmpty()) R.color.colorAccent else R.color.cardview_light_background))
            binding.tvBtnSend.setTextColor(ContextCompat.getColor(applicationContext, if (text.isNotEmpty()) android.R.color.white else android.R.color.darker_gray))
        }
        binding.rvPost.apply {
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
                    TYPE_ARTICLE -> HeaderHolder(PostDetailBinding.inflate(layoutInflater, parent, false)).also { headerHolder = it }
                    TYPE_REPLY -> ItemHolder(ItemReplyBinding.inflate(layoutInflater, parent, false))
                    else -> throw RuntimeException()
                }

                override fun getItemCount(): Int = viewModel.itemList.size

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    if (holder is HeaderHolder) {
                        holder.bind(viewModel.itemList[position] as PostItem.Post)
                    } else if (holder is ItemHolder) {
                        holder.bind(viewModel.itemList[position] as ReplyItem)
                    }
                }

                override fun getItemViewType(position: Int): Int = if (viewModel.itemList[position] is ReplyItem) TYPE_REPLY else TYPE_ARTICLE
            }
        }
        fetchArticleData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == POST_INFO_CODE && resultCode == Activity.RESULT_OK) {
            viewModel.isUpdate = true

            viewModel.itemList.clear()
            fetchArticleData()
        } else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val position = data!!.getIntExtra("position", 0)

            (viewModel.itemList[position] as ReplyItem).run {
                reply = data.getStringExtra("reply")
                viewModel.itemList[position] = this

                binding.rvPost.adapter?.notifyItemChanged(position)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {

            // 조건을 위해 xml레이아웃을 사용하지 않고 코드로 옵션메뉴를 구성함
            if (viewModel.post.userId == viewModel.user.id) {
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
                putExtra("post", viewModel.itemList[0] as PostItem.Post)
            }

            startActivityForResult(intent, POST_INFO_CODE)
            true
        }
        2 -> {
            val tagStringReq = "req_delete"
            val stringRequest = object : StringRequest(Method.DELETE, "${URLs.URL_POST}/${viewModel.post.id}", Response.Listener { response ->
                hideProgressBar()
                try {
                    val jsonObject = JSONObject(response)

                    if (!jsonObject.getBoolean("error")) {
                        setResult(Activity.RESULT_OK)
                        finish()
                        Toast.makeText(applicationContext, getString(R.string.delete_complete), Toast.LENGTH_LONG).show()
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
                override fun getHeaders() = mapOf("Authorization" to viewModel.user.apiKey)
            }

            showProgressBar()
            AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.groupId) {
        0 -> {
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).text = if (viewModel.itemList[item.itemId] is PostItem.Post) (viewModel.itemList[item.itemId] as PostItem.Post).text else (viewModel.itemList[item.itemId] as ReplyItem).reply

            Toast.makeText(applicationContext, "클립보드에 복사되었습니다!", Toast.LENGTH_LONG).show()
            true
        }
        1 -> {
            val intent = Intent(this, ReplyModifyActivity::class.java)
                .putExtra("position", item.itemId)
                .putExtra("reply", viewModel.itemList[item.itemId] as ReplyItem)

            startActivityForResult(intent, REQUEST_CODE)
            true
        }
        2 -> {
            val replyId = (viewModel.itemList[item.itemId] as ReplyItem).id
            val tagStringReq = "req_delete"
            val stringRequest = object : StringRequest(Method.DELETE, URLs.URL_REPLY.replace("{REPLY_ID}", replyId.toString()), Response.Listener { response ->
                hideProgressBar()
                try {
                    val jsonObject = JSONObject(response)

                    if (!jsonObject.getBoolean("error")) {
                        viewModel.itemList.removeAt(item.itemId)
                        binding.rvPost.adapter?.notifyItemRemoved(item.itemId)
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
                override fun getHeaders() = hashMapOf("Authorization" to viewModel.user.apiKey)
            }

            showProgressBar()
            AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
            true
        }
        else -> super.onContextItemSelected(item)
    }

    private fun initialize() {
        viewModel.post = intent.getParcelableExtra("post")!!
        viewModel.isBottom = intent.getBooleanExtra("is_bottom", false)
        viewModel.position = intent.getIntExtra("position", 0)
        viewModel.groupName = intent.getStringExtra("group_name")
    }

    private fun fetchArticleData() {
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, "${URLs.URL_POST}/${viewModel.post.id}", null,  Response.Listener { response ->
            hideProgressBar()
            try {
                PostItem.Post().run {
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
                    viewModel.itemList.add(0, this)
                    if (viewModel.isUpdate)
                        deliveryUpdate(this)
                }
                binding.rvPost.adapter?.notifyDataSetChanged()
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
        val jsonArrayRequest = object : JsonArrayRequest(Method.GET, URLs.URL_REPLYS.replace("{POST_ID}", viewModel.post.id.toString()), null, Response.Listener { response ->
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
                        viewModel.itemList.add(this)
                    }
                    binding.rvPost.adapter?.notifyItemChanged(i + 1)
                }
                if (viewModel.isBottom)
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
            override fun getHeaders() = mapOf("Authorization" to viewModel.user.apiKey)
        }

        AppController.getInstance().addToRequestQueue(jsonArrayRequest)
    }

    private fun moveToBottom() {
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.isBottom = false

            binding.rvPost.scrollToPosition(viewModel.itemList.size - 1)
        }, 300)
    }

    private fun deliveryUpdate(post: PostItem.Post) {
        val intent = Intent(this, Tab1Fragment::class.java).apply {
            with(post) {
                putExtra("text", text)
                putParcelableArrayListExtra("images", imageItemList as ArrayList<out Parcelable>)
                putExtra("reply_count", replyCount)
                putExtra("position", viewModel.position)
            }
        }

        setResult(POST_INFO_CODE, intent)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    inner class HeaderHolder(val binding: PostDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnLongClickListener { v ->
                v.apply {
                    setOnCreateContextMenuListener { menu, _, _ ->
                        menu.apply {
                            setHeaderTitle(getString(R.string.select_action))
                            add(0, adapterPosition, Menu.NONE, getString(R.string.copy_content))
                        }
                    }
                    showContextMenu()
                }
                true
            }
        }

        fun bind(post: PostItem.Post) = with(binding) {
            tvName.text = post.name
            tvCreateAt.text = Utils.getPeriodTimeGenerator(root.context, post.timeStamp)

            if (!TextUtils.isEmpty(post.text)) {
                tvText.text = post.text
                tvText.visibility = View.VISIBLE
            } else
                tvText.visibility = View.GONE
            if (post.imageItemList.isNotEmpty()) {
                llImage.visibility = View.VISIBLE

                llImage.removeAllViews()
                post.imageItemList.forEachIndexed { index, imageItem ->
                    ImageView(root.context).apply {
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.FIT_XY

                        setPadding(0, 0, 0, 30)
                        Glide.with(context)
                            .load("${URLs.URL_POST_IMAGE_PATH}${imageItem.image}")
                            .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                            .into(this)
                        setOnClickListener {
                            Intent(baseContext, PictureActivity::class.java)
                                .putParcelableArrayListExtra("images", post.imageItemList as ArrayList<out Parcelable>)
                                .putExtra("position", index)
                                .also(::startActivity)
                        }
                    }.also { llImage.addView(it) } // apply().also() -> run()으로 바꿀수 있음
                }
            } else
                llImage.visibility = View.GONE
            tvLikeCount.text = post.likeCount.toString()
            tvReplyCount.text = post.replyCount.toString()

            Glide.with(root.context)
                .load("${URLs.URL_USER_PROFILE_IMAGE}${post.profileImage}")
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(ivProfileImage)
        }
    }

    inner class ItemHolder(val binding: ItemReplyBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnLongClickListener { v ->
                v.apply {
                    setOnCreateContextMenuListener { menu, _, _ ->
                        menu.apply {
                            setHeaderTitle(getString(R.string.select_action))
                            add(0, adapterPosition, Menu.NONE, getString(R.string.copy_content))
                            if ((viewModel.itemList[adapterPosition] as ReplyItem).userId == viewModel.user.id) {
                                add(1, adapterPosition, Menu.NONE, getString(R.string.edit_comment))
                                add(2, adapterPosition, Menu.NONE, getString(R.string.delete_comment))
                            }
                        }
                    }
                    showContextMenu()
                }
                true
            }
        }

        fun bind(replyItem: ReplyItem) = with(binding) {
            tvName.text = replyItem.name
            tvReply.text = replyItem.reply
            tvCreateAt.text = Utils.getPeriodTimeGenerator(root.context, replyItem.timeStamp)

            Glide.with(root.context)
                .load("${URLs.URL_USER_PROFILE_IMAGE}${(replyItem.profileImage)}")
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(ivProfileImage)
        }
    }

    companion object {
        const val REQUEST_CODE = 0
        private const val TYPE_ARTICLE = 10
        private const val TYPE_REPLY = 20
        private val TAG = PostDetailActivity::class.simpleName
    }
}