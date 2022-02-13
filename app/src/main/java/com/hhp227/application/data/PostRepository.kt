package com.hhp227.application.data

import android.graphics.Bitmap
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.ImageItem
import com.hhp227.application.dto.PostItem
import com.hhp227.application.util.Resource
import com.hhp227.application.volley.util.MultipartRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
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

    fun addPost(apiKey: String, groupId: Int, text: String) = callbackFlow<Resource<Int>> {
        val tagStringReq = "req_insert"
        val stringRequest = object : StringRequest(Method.POST, URLs.URL_POST, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {
                    val postId = jsonObject.getInt("post_id")

                    trySendBlocking(Resource.Success(postId))
                } else {
                    trySendBlocking(Resource.Error(jsonObject.getString("message")))
                }
            } catch (e: JSONException) {
                trySendBlocking(Resource.Error(e.message.toString()))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)

            override fun getParams() = mapOf("text" to text, "group_id" to groupId.toString())
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
        awaitClose { close() }
    }

    fun setPost(apiKey: String, postId: Int, text: String) = callbackFlow<Resource<Int>> {
        val tagStringReq = "req_update"
        val stringRequest = object : StringRequest(Method.PUT, "${URLs.URL_POST}/$postId", Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {
                    trySendBlocking(Resource.Success(postId))
                } else {
                    trySendBlocking(Resource.Error(response))
                }
            } catch (e: JSONException) {
                trySendBlocking(Resource.Error(e.message.toString()))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)

            override fun getParams() = mapOf("text" to text, "status" to "0")
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
        awaitClose { close() }
    }

    fun addPostImage(apiKey: String, postId: Int, bitmap: Bitmap) = callbackFlow<Resource<String>> {
        val multiPartRequest = object : MultipartRequest(Method.POST, URLs.URL_POST_IMAGE, Response.Listener { response ->
            val jsonObject = JSONObject(String(response.data))

            if (!jsonObject.getBoolean("error")) {
                trySendBlocking(Resource.Success(jsonObject.getString("image")))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)

            override fun getByteData() = mapOf(
                "image" to DataPart("${System.currentTimeMillis()}.jpg", ByteArrayOutputStream().also {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
                }.toByteArray())
            )

            override fun getParams() = mapOf("post_id" to postId.toString())
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(multiPartRequest)
        awaitClose { close() }
    }

    fun removePostImages(apiKey: String, postId: Int, jsonArray: JSONArray) = callbackFlow<Resource<String>> {
        val tagStringReq = "req_delete_image"
        val stringRequest = object : StringRequest(Method.POST, URLs.URL_POST_IMAGE_DELETE, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {
                    trySendBlocking(Resource.Success(jsonObject.getString("ids")))
                } else {
                    trySendBlocking(Resource.Error(response))
                }
            } catch (e: JSONException) { // TODO 지울것
                Log.e("TEST", "ErrorOccured: ${e.message}, jsonArray: $jsonArray")
                trySendBlocking(Resource.Error(e.message.toString()))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)

            override fun getParams() = mapOf("ids" to jsonArray.toString(), "post_id" to postId.toString())
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
        awaitClose { close() }
    }
}