package com.hhp227.application.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.Request
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.dto.ImageItem
import com.hhp227.application.dto.PostItem
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.Tab2ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import kotlin.jvm.Throws

class Tab2Fragment : Fragment() {
    private val viewModel: Tab2ViewModel by viewModels()

    private var binding: FragmentTabBinding by autoCleared()

    private var offset = 0

    private var hasRequestedMore = false // 데이터 불러올때 중복안되게 하기위한 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.groupId = it.getInt(ARG_PARAM1)
            viewModel.groupName = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 처음 캐시메모리 요청을 체크
        val cache = AppController.getInstance().requestQueue.cache
        val entry = cache[URLs.URL_ALBUM]

        binding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                delay(1000)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL)
        }
        showProgressBar()
        entry?.let {
            // 캐시메모리에서 데이터 인출
            try {
                val data = String(it.data, Charsets.UTF_8)

                try {
                    parseJson(JSONObject(data))
                    hideProgressBar()
                } catch (e: JSONException) {
                    Log.e(TAG, "에러$e")
                }
            } catch (e: UnsupportedEncodingException) {
                Log.e(TAG, "에러$e")
            }
        } ?: fetchAlbumList()
    }

    @Throws(JSONException::class)
    private fun parseJson(jsonObject: JSONObject) {
        jsonObject.getJSONArray("posts").also { jsonArr ->
            hasRequestedMore = false

            Log.e("TEST", "jsonArr: $jsonArr")
            for (i in 0 until jsonArr.length()) {
                viewModel.postItems.add(viewModel.postItems.size - 1, PostItem.Post().apply {
                    with(jsonArr.getJSONObject(i)) {
                        id = getInt("id")
                        userId = getInt("user_id")
                        name = getString("name")
                        text = getString("text")
                        profileImage = getString("profile_img")
                        timeStamp = getString("created_at")
                        replyCount = getInt("reply_count")
                        likeCount = getInt("like_count")
                        imageItemList = getJSONObject("attachment").getJSONArray("images").let { images ->
                            List(images.length()) { j ->
                                ImageItem().apply {
                                    with(images.getJSONObject(j)) {
                                        id = getInt("id")
                                        image = getString("image")
                                        tag = getString("tag")
                                    }
                                }
                            }
                        }
                    }
                })
                binding.recyclerView.adapter?.notifyItemInserted(viewModel.postItems.size - 1)
            }
        }
    }

    private fun fetchAlbumList() {
        val jsonReq = JsonObjectRequest(Request.Method.GET, URLs.URL_ALBUM.replace("{OFFSET}", offset.toString()).replace("{GROUP_ID}", viewModel.groupId.toString()), null, { response ->
            VolleyLog.d(TAG, "응답: $response")
            if (response != null) {
                parseJson(response)
                hideProgressBar()
            }
        }) { error ->
            VolleyLog.d(TAG, "Volley에러: " + error.message)
            hideProgressBar()
        }

        AppController.getInstance().addToRequestQueue(jsonReq)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    companion object {
        private const val ARG_PARAM1 = "group_id"
        private const val ARG_PARAM2 = "group_name"
        private val TAG = Tab2Fragment::class.java.simpleName

        fun newInstance(groupId: Int, groupName: String) =
            Tab2Fragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, groupId)
                    putString(ARG_PARAM2, groupName)
                }
            }
    }
}