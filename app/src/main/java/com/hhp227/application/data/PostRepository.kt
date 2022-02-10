package com.hhp227.application.data

import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.ImageItem
import com.hhp227.application.dto.PostItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class PostRepository {
    private fun getCachedData(url: String): Resource<List<PostItem>>? {
        return AppController.getInstance().requestQueue.cache[url]?.let { entry ->
            // 캐시메모리에서 데이터 인출
            try {
                val data = String(entry.data, Charsets.UTF_8)

                try {
                    Log.e("TEST", "getCachedData")
                    Resource.Success(parseJson(JSONObject(data)))
                } catch (e: JSONException) {
                    Resource.Error(e.message.toString())
                }
            } catch (e: UnsupportedEncodingException) {
                Resource.Error(e.message.toString())
            }
        } ?: return null
    }

    @Throws(JSONException::class)
    private fun parseJson(jsonObject: JSONObject): List<PostItem> {
        val postItems = mutableListOf<PostItem>()
        val jsonArray = jsonObject.getJSONArray("posts")

        for (i in 0 until jsonArray.length()) {
            with(jsonArray.getJSONObject(i)) {
                val postItem = PostItem.Post(
                    id = getInt("id"),
                    userId = getInt("user_id"),
                    name = getString("name"),
                    text = getString("text"),
                    profileImage = getString("profile_img"),
                    timeStamp = getString("created_at"),
                    replyCount = getInt("reply_count"),
                    likeCount = getInt("like_count"),
                    imageItemList = getJSONObject("attachment").getJSONArray("images").let { images ->
                        ArrayList<ImageItem>().also { imageList ->
                            for (j in 0 until images.length()) {
                                with(images.getJSONObject(j)) {
                                    imageList += ImageItem(
                                        id = getInt("id"),
                                        image = getString("image"),
                                        tag = getString("tag")
                                    )
                                }
                            }
                        }
                    }
                )

                postItems.add(/*mItemList.size - 1, */postItem)
            }
        }
        return postItems
    }

    private fun refreshCachedData(url: String) {
        AppController.getInstance().requestQueue.cache.remove(url)
        AppController.getInstance().requestQueue.cache.invalidate(url, true)
    }

    fun getPostList(groupId: Int, offset: Int) = callbackFlow<Resource<List<PostItem>>> {
        val url = URLs.URL_POSTS.replace("{GROUP_ID}", groupId.toString()).replace("{OFFSET}", offset.toString())
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, url, null, Response.Listener { response ->
            if (response != null) {
                trySendBlocking(Resource.Success(parseJson(response)))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf(
                "Content-Type" to "application/json",
                "api_key" to "xxxxxxxxxxxxxxx"
            )
        }

        trySend(Resource.Loading())

        // TODO Cache데이터는 나중에 처리할것
        /*getCachedData(url)?.also(::trySend) ?: */AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose { close() }
    }

    fun refreshPostList(groupId: Int, offset: Int) {
        val url = URLs.URL_ALBUM.replace("{GROUP_ID}", groupId.toString()).replace("{OFFSET}", offset.toString())

        refreshCachedData(url)
    }

    fun getPostListWithImage(groupId: Int, offset: Int) = callbackFlow<Resource<List<PostItem>>> {
        val url = URLs.URL_ALBUM.replace("{GROUP_ID}", groupId.toString()).replace("{OFFSET}", offset.toString())
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            if (response != null) {
                trySendBlocking(Resource.Success(parseJson(response)))
            }
        }) { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }

        trySend(Resource.Loading())
        /*getCachedData(url)?.also(::trySend) ?: */AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose { close() }
    }
}