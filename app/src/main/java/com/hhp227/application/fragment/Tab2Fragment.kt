package com.hhp227.application.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.util.autoCleared
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import kotlin.jvm.Throws

class Tab2Fragment : Fragment() {
    private var binding: FragmentTabBinding by autoCleared()

    private var offset: Int = 0

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
    private fun parseJson(response: JSONObject) {
        val albumArray: JSONArray = response.getJSONArray("album")

        for (i in 0 until albumArray.length()) {
            val albumObj = albumArray[i] as JSONObject
            val imageUrl = albumObj.getString("image")
            Log.e("확인", imageUrl)
            offset++
        }
    }

    private fun fetchAlbumList() {
        val jsonReq = JsonObjectRequest(Request.Method.GET, URLs.URL_ALBUM + offset, null, { response ->
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
        private const val PICK_IMAGE_REQUEST_CODE = 100
        private val TAG = Tab2Fragment::class.java.simpleName

        fun newInstance() = Tab2Fragment()
    }
}