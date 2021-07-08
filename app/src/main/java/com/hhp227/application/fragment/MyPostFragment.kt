package com.hhp227.application.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.hhp227.application.R
import com.hhp227.application.adapter.PostListAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.dto.ImageItem
import com.hhp227.application.dto.PostItem
import com.hhp227.application.util.autoCleared
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import kotlin.jvm.Throws

class MyPostFragment : Fragment() {
    private val postItems by lazy { arrayListOf(Any()) }

    private var offset = 0

    private var binding: FragmentTabBinding by autoCleared()

    private var hasRequestedMore = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val entry = AppController.getInstance().requestQueue.cache[URLs.URL_USER_POSTS]

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PostListAdapter().apply {
                setLoaderVisibility(View.INVISIBLE)
                submitList(postItems)
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (!hasRequestedMore && dy > 0 && layoutManager != null && (layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() >= layoutManager!!.itemCount - 1) {
                            hasRequestedMore = true
                            offset = postItems.size - 1

                            setLoaderVisibility(View.VISIBLE)
                            notifyItemChanged(postItems.size - 1)
                            fetchPostList()
                        }
                    }
                })
                setOnItemClickListener { v, p ->
                    Log.e("TEST", "${currentList[p]}")
                }
            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                delay(1000)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        showProgressBar()
        entry?.let {
            // 캐시메모리에서 데이터 인출
            try {
                val data = String(it.data, Charsets.UTF_8)

                try {
                    parseJsonArray(JSONArray(data))
                    hideProgressBar()
                } catch (e: JSONException) {
                    Log.e(TAG, "에러$e")
                }
            } catch (e: UnsupportedEncodingException) {
                Log.e(TAG, "에러$e")
            }
        } ?: fetchPostList()
    }

    private fun fetchPostList() {
        val jsonArrayRequest = object : JsonArrayRequest(Method.GET, URLs.URL_USER_POSTS.replace("{OFFSET}", "$offset"), null, Response.Listener { response ->
            response?.let {
                parseJsonArray(it)
                hideProgressBar()
            }
        }, Response.ErrorListener { error ->
            VolleyLog.e(TAG, "Volley에러: ${error.message}")
            (binding.recyclerView.adapter as PostListAdapter).setLoaderVisibility(View.GONE)
            binding.recyclerView.adapter?.notifyItemChanged(postItems.size - 1)
            hideProgressBar()
        }) {
            override fun getHeaders() = mapOf(
                "Authorization" to AppController.getInstance().preferenceManager.user.apiKey
            )
        }

        AppController.getInstance().addToRequestQueue(jsonArrayRequest)
    }

    @Throws(JSONException::class)
    private fun parseJsonArray(jsonArray: JSONArray) {
        hasRequestedMore = false

        for (i in 0 until jsonArray.length()) {
            with(jsonArray.getJSONObject(i)) {
                if (!getBoolean("error")) {
                    val postItem = PostItem(
                        id = getInt("id"),
                        userId = getInt("user_id"),
                        name = getString("name"),
                        text = getString("text"),
                        status = getString("status"),
                        profileImage = getString("profile_img"),
                        timeStamp = getString("created_at"),
                        replyCount = getInt("reply_count"),
                        likeCount = getInt("like_count"),
                        imageItemList = getJSONObject("attachment").getJSONArray("images").let { images ->
                            List(images.length()) { j ->
                                with(images.getJSONObject(j)) {
                                    ImageItem(
                                        id = getInt("id"),
                                        image = getString("image"),
                                        tag = getString("tag")
                                    )
                                }
                            }
                        }
                    )

                    postItems.add(postItems.size - 1, postItem)
                    binding.recyclerView.adapter?.notifyItemInserted(postItems.size - 1)
                }
            }
        }
        (binding.recyclerView.adapter as PostListAdapter).setLoaderVisibility(View.INVISIBLE)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    companion object {
        private val TAG = MyPostFragment::class.java.simpleName

        fun newInstance(): Fragment = MyPostFragment()
    }
}