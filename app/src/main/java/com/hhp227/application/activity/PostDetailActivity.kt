package com.hhp227.application.activity

import com.hhp227.application.fragment.PostFragment
import com.hhp227.application.fragment.PostFragment.Companion.POST_INFO_CODE
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.adapter.ReplyListAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.data.PostRepository
import com.hhp227.application.data.ReplyRepository
import com.hhp227.application.databinding.ActivityPostBinding
import com.hhp227.application.databinding.ItemReplyBinding
import com.hhp227.application.databinding.PostDetailBinding
import com.hhp227.application.dto.*
import com.hhp227.application.util.Utils
import com.hhp227.application.viewmodel.PostDetailViewModel
import com.hhp227.application.viewmodel.PostDetailViewModelFactory
import com.hhp227.application.viewmodel.WriteViewModel.Companion.TYPE_UPDATE
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.json.JSONException
import org.json.JSONObject

class PostDetailActivity : AppCompatActivity() {
    private val viewModel: PostDetailViewModel by viewModels {
        PostDetailViewModelFactory(PostRepository(), ReplyRepository(), this, intent.extras)
    }

    private lateinit var binding: ActivityPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.run {
            title = if (TextUtils.isEmpty(viewModel.groupName)) getString(R.string.lounge_fragment) else viewModel.groupName

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
            adapter = ReplyListAdapter().apply {
                submitList(viewModel.itemList)
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

            (viewModel.itemList[position] as ReplyItem.Reply).run {
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
            (viewModel.itemList[0] as ReplyItem.Post).also { (id, userId, name, text, imageItemList, status, profileImage, timeStamp, replyCount, likeCount) ->
                val postItem = PostItem.Post(id, userId, name, text, imageItemList, status, profileImage, timeStamp, replyCount, likeCount)
                val intent = Intent(this, WriteActivity::class.java).apply {
                    putExtra("type", TYPE_UPDATE)
                    putExtra("post", postItem)
                }

                startActivityForResult(intent, POST_INFO_CODE)
                //startActivityForResult(intent, RESULT_OK)
            }
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
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).text = if (viewModel.itemList[item.itemId] is ReplyItem.Post) (viewModel.itemList[item.itemId] as ReplyItem.Post).text else (viewModel.itemList[item.itemId] as ReplyItem.Reply).reply

            Toast.makeText(applicationContext, "클립보드에 복사되었습니다!", Toast.LENGTH_LONG).show()
            true
        }
        1 -> {
            val intent = Intent(this, ReplyModifyActivity::class.java)
                .putExtra("position", item.itemId)
                .putExtra("reply", viewModel.itemList[item.itemId] as ReplyItem.Reply)

            startActivityForResult(intent, REQUEST_CODE)
            true
        }
        2 -> {
            val replyId = (viewModel.itemList[item.itemId] as ReplyItem.Reply).id
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

    // TODO 이미지 업데이트 하면 이미지가 두번 불려와짐 텍스트 업데이트는 한번만 불러오는데 이미지 업데이트만 하면 두개가 쌓임
    private fun fetchArticleData() {
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, "${URLs.URL_POST}/${viewModel.post.id}", null,  Response.Listener { response ->
            hideProgressBar()
            try {
                ReplyItem.Post().run {
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
                    ReplyItem.Reply().run {
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

    private fun deliveryUpdate(post: ReplyItem.Post) {
        val postItem = PostItem.Post(
            post.id,
            post.userId,
            post.name,
            post.text,
            post.imageItemList,
            post.status,
            post.profileImage,
            post.timeStamp,
            post.replyCount,
            post.likeCount
        )
        val intent = Intent(this, PostFragment::class.java).putExtra("post", postItem)

        setResult(POST_INFO_CODE, intent)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    companion object {
        const val REQUEST_CODE = 0
        private val TAG = PostDetailActivity::class.simpleName
    }
}

/*class PostDetailActivity : AppCompatActivity() {
    private val viewModel: PostDetailViewModel by viewModels {
        PostDetailViewModelFactory(PostRepository(), ReplyRepository(), this, intent.extras)
    }

    private lateinit var binding: ActivityPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        binding.rvPost.adapter = ReplyListAdapter()

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.run {
            title = if (TextUtils.isEmpty(viewModel.groupName)) getString(R.string.lounge_fragment) else viewModel.groupName

            setDisplayHomeAsUpEnabled(true)
        }
        binding.srlPost.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.srlPost.isRefreshing = false

                // TODO
            }, 1000)
        }
        binding.cvBtnSend.setOnClickListener { v -> viewModel.insertReply(binding.etReply.text.toString().trim()) }
        binding.etReply.doOnTextChanged { text, _, _, _ ->
            binding.cvBtnSend.setCardBackgroundColor(ContextCompat.getColor(applicationContext, if (text!!.isNotEmpty()) R.color.colorAccent else R.color.cardview_light_background))
            binding.tvBtnSend.setTextColor(ContextCompat.getColor(applicationContext, if (text.isNotEmpty()) android.R.color.white else android.R.color.darker_gray))
        }
        fetchArticleData()
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.replyId >= 0 -> {
                    Toast.makeText(applicationContext, "전송 완료", Toast.LENGTH_LONG).show()
                    /*viewModel.itemList.clear()
                    fetchArticleData()*/

                    // 전송할때마다 하단으로
                    moveToBottom()
                    binding.etReply.setText("")
                    binding.cvBtnSend.let { (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(it.windowToken, 0) }
                }
            }
        }.launchIn(lifecycleScope)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == POST_INFO_CODE && resultCode == Activity.RESULT_OK) {
            viewModel.isUpdate = true

            viewModel.itemList.clear()
            fetchArticleData()
        } else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val position = data!!.getIntExtra("position", 0)

            (viewModel.itemList[position] as ReplyItem.Reply).run {
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
            //startActivityForResult(intent, RESULT_OK)
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
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).text = if (viewModel.itemList[item.itemId] is PostItem.Post) (viewModel.itemList[item.itemId] as PostItem.Post).text else (viewModel.itemList[item.itemId] as ReplyItem.Reply).reply

            Toast.makeText(applicationContext, "클립보드에 복사되었습니다!", Toast.LENGTH_LONG).show()
            true
        }
        1 -> {
            val intent = Intent(this, ReplyModifyActivity::class.java)
                .putExtra("position", item.itemId)
                .putExtra("reply", viewModel.itemList[item.itemId] as ReplyItem.Reply)

            startActivityForResult(intent, REQUEST_CODE)
            true
        }
        2 -> {
            val replyId = (viewModel.itemList[item.itemId] as ReplyItem.Reply).id
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

    // TODO 이미지 업데이트 하면 이미지가 두번 불려와짐 텍스트 업데이트는 한번만 불러오는데 이미지 업데이트만 하면 두개가 쌓임
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
                    ReplyItem.Reply().run {
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
        val intent = Intent(this, PostFragment::class.java)
            .putExtra("post", post)

        setResult(POST_INFO_CODE, intent)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    companion object {
        const val REQUEST_CODE = 0
        private val TAG = PostDetailActivity::class.simpleName
    }
}*/