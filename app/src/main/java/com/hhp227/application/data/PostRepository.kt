package com.hhp227.application.data

import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.ImageItem
import com.hhp227.application.dto.PostItem
import com.hhp227.application.fragment.Tab2Fragment
import com.hhp227.application.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class PostRepository {
    fun getPostList(groupId: Int, offset: Int) = callbackFlow<Resource<List<PostItem>>> {
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, URLs.URL_POSTS.replace("{GROUP_ID}", groupId.toString()).replace("{OFFSET}", offset.toString()), null, Response.Listener { response ->
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
        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose { close() }
    }

    fun getPostWithImage(groupId: Int, offset: Int) = callbackFlow<Resource<List<PostItem>>> {
        val url = URLs.URL_ALBUM.replace("{GROUP_ID}", groupId.toString()).replace("{OFFSET}", offset.toString())
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            if (response != null) {
                trySendBlocking(Resource.Success(parseJson(response)))
            }
        }) { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }

        trySend(Resource.Loading())
        AppController.getInstance().requestQueue.cache[url]?.let { entry ->
            // 캐시메모리에서 데이터 인출
            try {
                val data = String(entry.data, Charsets.UTF_8)

                try {
                    trySend(Resource.Success(parseJson(JSONObject(data))))
                } catch (e: JSONException) {
                    trySend(Resource.Error(e.message.toString()))
                }
            } catch (e: UnsupportedEncodingException) {
                trySend(Resource.Error(e.message.toString()))
            }
        } ?: AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose { close() }
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
}