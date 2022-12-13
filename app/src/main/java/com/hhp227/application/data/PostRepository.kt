package com.hhp227.application.data

import android.graphics.Bitmap
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.api.ApiService
import com.hhp227.application.app.AppController
import com.hhp227.application.util.URLs
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.Resource
import com.hhp227.application.volley.util.MultipartRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException

class PostRepository(private val apiService: ApiService) {
    private fun getCachedData(url: String): Resource<List<ListItem>>? {
        return AppController.getInstance().requestQueue.cache[url]?.let { entry ->
            // 캐시메모리에서 데이터 인출
            try {
                val data = String(entry.data, Charsets.UTF_8)

                try {
                    Log.e("TEST", "getCachedData")
                    Resource.Success(parsePostList(JSONObject(data).getJSONArray("posts")))
                } catch (e: JSONException) {
                    Resource.Error(e.message.toString())
                }
            } catch (e: UnsupportedEncodingException) {
                Resource.Error(e.message.toString())
            }
        } ?: return null
    }

    @Throws(JSONException::class)
    private fun parsePostList(jsonArray: JSONArray): List<ListItem> {
        return List(jsonArray.length()) { i ->
            parsePost(jsonArray.getJSONObject(i))
        }
    }

    private fun parsePost(jsonObject: JSONObject): ListItem.Post {
        return ListItem.Post(
            id = jsonObject.getInt("id"),
            userId = jsonObject.getInt("user_id"),
            name = jsonObject.getString("name"),
            text = jsonObject.getString("text"),
            profileImage = jsonObject.getString("profile_img"),
            timeStamp = jsonObject.getString("created_at"),
            replyCount = jsonObject.getInt("reply_count"),
            likeCount = jsonObject.getInt("like_count"),
            reportCount = jsonObject.getInt("report_count"),
            attachment = jsonObject.getJSONObject("attachment").let { jsonObj ->
                ListItem.Attachment(
                    imageItemList = jsonObj.getJSONArray("images").let { images ->
                        List(images.length()) { j ->
                            ListItem.Image(
                                id = images.getJSONObject(j).getInt("id"),
                                image = images.getJSONObject(j).getString("image"),
                                tag = images.getJSONObject(j).getString("tag")
                            )
                        }
                    },
                    video = jsonObj.getString("video")
                )
            }
        )
    }

    private fun refreshCachedData(url: String) {
        AppController.getInstance().requestQueue.cache.remove(url)
        AppController.getInstance().requestQueue.cache.invalidate(url, true)
    }

    fun getPostList(groupId: Int): Flow<PagingData<ListItem.Post>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 10),
            pagingSourceFactory = { PostPagingSource(apiService, groupId) },
        ).flow
    }

    fun getPostList(groupId: Int, offset: Int) = callbackFlow<Resource<List<ListItem>>> {
        val url = URLs.URL_POSTS.replace("{GROUP_ID}", groupId.toString()).replace("{OFFSET}", offset.toString())
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, url, null, Response.Listener { response ->
            if (response != null) {
                trySendBlocking(Resource.Success(parsePostList(response.getJSONArray("posts"))))
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
    /*fun getPostList(groupId: Int, offset: Int) = callbackFlow<Resource<List<ListItem>>> {
        try {
            val posts = apiService.getPostList(
                groupId = groupId,
                offset = offset
            ).posts
            send(Resource.Success(data = posts))
        } catch(e: IOException) {
            send(Resource.Error(
                e.message!!
            ))
        } catch(e: HttpException) {
            send(Resource.Error(
                e.message()
            ))
        }
        awaitClose { close() }
    }*/

    fun refreshPostList(groupId: Int, offset: Int) {
        val url = URLs.URL_ALBUM.replace("{GROUP_ID}", groupId.toString()).replace("{OFFSET}", offset.toString())

        refreshCachedData(url)
    }

    fun getPostListWithImage(groupId: Int, offset: Int) = callbackFlow<Resource<List<ListItem>>> {
        val url = URLs.URL_ALBUM.replace("{GROUP_ID}", groupId.toString()).replace("{OFFSET}", offset.toString())
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            if (response != null) {
                trySendBlocking(Resource.Success(parsePostList(response.getJSONArray("posts"))))
            }
        }) { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }

        trySend(Resource.Loading())
        /*getCachedData(url)?.also(::trySend) ?: */AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose { close() }
    }

    fun getUserPostList(apiKey: String, offset: Int) = callbackFlow<Resource<List<ListItem>>>{
        val jsonArrayRequest = object : JsonArrayRequest(Method.GET, URLs.URL_USER_POSTS.replace("{OFFSET}", "$offset"), null, Response.Listener { response ->
            response?.let(::parsePostList)?.also { list ->
                trySendBlocking(Resource.Success(list))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf(
                "Authorization" to apiKey
            )
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonArrayRequest)
        awaitClose { close() }
    }

    fun getPost(postId: Int) = callbackFlow<Resource<ListItem.Post>> {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, "${URLs.URL_POST}/${postId}", null, { response ->
            try {
                trySendBlocking(Resource.Success(parsePost(response)))
            } catch (e: JSONException) {
                trySendBlocking(Resource.Error(e.message.toString()))
            }
        }, { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        })

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
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

    fun removePost(apiKey: String, postId: Int) = callbackFlow<Resource<Boolean>> {
        val tagStringReq = "req_delete"
        val stringRequest = object : StringRequest(Method.DELETE, "${URLs.URL_POST}/${postId}", Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {
                    trySendBlocking(Resource.Success(true))
                }
            } catch (e: JSONException) {
                trySendBlocking(Resource.Error(e.message.toString()))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)
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
            val jsonObject = JSONObject(response)

            if (!jsonObject.getBoolean("error")) {
                trySendBlocking(Resource.Success(jsonObject.getString("ids")))
            } else {
                trySendBlocking(Resource.Error(response))
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

    fun toggleLike(apiKey: String, postId: Int) = callbackFlow<Resource<String>> {
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, URLs.URL_POST_LIKE.replace("{POST_ID}", postId.toString()), null, Response.Listener { response ->
            if (!response.getBoolean("error")) {
                trySendBlocking(Resource.Success(response.getString("result")))
            } else {
                trySendBlocking(Resource.Error(response.getString("message")))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders(): MutableMap<String, String?> = hashMapOf("Authorization" to apiKey)
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose { close() }
    }

    fun toggleReport(apiKey: String, postId: Int) = callbackFlow<Resource<String>> {
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, URLs.URL_POST_REPORT.replace("{POST_ID}", postId.toString()), null, Response.Listener { response ->
            if (!response.getBoolean("error")) {
                trySendBlocking(Resource.Success(response.getString("result")))
            } else {
                trySendBlocking(Resource.Error(response.getString("message")))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders(): MutableMap<String, String?> = hashMapOf("Authorization" to apiKey)
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose { close() }
    }

    companion object {
        @Volatile private var instance: PostRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: PostRepository(ApiService.create()).also { instance = it }
            }
    }
}