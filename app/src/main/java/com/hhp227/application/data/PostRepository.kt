package com.hhp227.application.data

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.api.PostService
import com.hhp227.application.app.AppController
import com.hhp227.application.util.URLs
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.Resource
import com.hhp227.application.volley.util.MultipartRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.IOException

// WIP
class PostRepository(private val postService: PostService) {

    fun getPostList(groupId: Int): LiveData<PagingData<ListItem.Post>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 10),
            pagingSourceFactory = { PostListPagingSource(postService, groupId) },
        ).liveData
    }

    // TODO PagingSource로 처리해야됨
    fun getPostList(groupId: Int, offset: Int) = flow<Resource<List<ListItem>>> {
        try {
            val response = postService.getPostList(
                groupId = groupId,
                page = offset / 10,
                loadSize = 10
            )

            if (!response.error) {
                emit(Resource.Success(response.data ?: emptyList()))
            }
        } catch(e: IOException) {
            emit(Resource.Error(e.message!!))
        } catch(e: HttpException) {
            emit(Resource.Error(e.message()))
        }
    }
        .onStart { emit(Resource.Loading()) }

    // TODO PagingSource로 처리해야됨
    fun getPostListWithImage(groupId: Int, offset: Int): Flow<Resource<out List<ListItem>>> = flow<Resource<out List<ListItem>>> {
        try {
            val response = postService.getPostListWithImage(
                groupId = groupId,
                page = offset / 10,
                loadSize = 10
            )

            if (!response.error) {
                emit(Resource.Success(response.data!!))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    // TODO PagingSource로 처리해야됨
    fun getUserPostList(apiKey: String, offset: Int): Flow<Resource<out List<ListItem>>> = flow {
        try {
            val response = postService.getUserPostList(
                apiKey,
                page = offset / 10,
                loadSize = 10
            )

            if (!response.error) {
                emit(Resource.Success(response.data!!))
            } else {
                emit(Resource.Error(response.message!!))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun getPost(postId: Int): Flow<Resource<ListItem.Post>> = flow {
        try {
            val response = postService.getPost(postId)

            if (!response.error) {
                emit(Resource.Success(response.data!!))
            } else {
                emit(Resource.Error(response.message!!))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun addPost(apiKey: String, groupId: Int, text: String): Flow<Resource<Int>> = flow {
        try {
            val response = postService.addPost(apiKey, text, groupId)

            if (!response.error) {
                emit(Resource.Success(response.data!!))
            } else {
                emit(Resource.Error(response.message!!))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun setPost(apiKey: String, postId: Int, text: String): Flow<Resource<Int>> = flow {
        try {
            val response = postService.setPost(apiKey, postId, text)

            if (!response.error) {
                emit(Resource.Success(response.data!!))
            } else {
                emit(Resource.Error(response.message!!))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun removePost(apiKey: String, postId: Int): Flow<Resource<Boolean>> = flow {
        try {
            val response = postService.removePost(apiKey, postId)

            if (!response.error) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(response.message!!, false))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    // TODO
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

    // TODO
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

    fun toggleLike(apiKey: String, postId: Int): Flow<Resource<out String>> = flow {
        try {
            val response = postService.togglePostLike(apiKey, postId)

            if (!response.error) {
                requireNotNull(response.data)
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Error(response.message ?: "", null))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun toggleReport(apiKey: String, postId: Int): Flow<Resource<out String>> = flow {
        try {
            val response = postService.toggleReport(apiKey, postId)

            if (!response.error) {
                requireNotNull(response.data)
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Error(response.message ?: "", null))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    companion object {
        @Volatile private var instance: PostRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: PostRepository(PostService.create()).also { instance = it }
            }
    }
}