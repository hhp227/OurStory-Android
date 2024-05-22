package com.hhp227.application.data

import android.graphics.Bitmap
import androidx.paging.*
import com.hhp227.application.api.PostService
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.IOException

class PostRepository(
    private val postService: PostService,
    private val localDataSource: PostDao
) {
    fun getPostList(groupId: Int): Flow<PagingData<ListItem.Post>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 10),
            pagingSourceFactory = { PostPagingSource(postService, localDataSource, groupId) },
        ).flow
    }

    // TODO PagingSource로 처리해야됨
    fun getPostListWithImage(groupId: Int, offset: Int): Flow<Resource<out List<ListItem>>> = flow<Resource<out List<ListItem>>> {
        try {
            val response = postService.getPostListWithImage(
                groupId = groupId,
                page = offset / 10,
                loadSize = 10
            )

            if (!response.error) {
                requireNotNull(response.data)
                emit(Resource.Success(response.data))
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
                requireNotNull(response.data)
                emit(Resource.Success(response.data))
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
                requireNotNull(response.data)
                emit(Resource.Success(response.data))
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
                requireNotNull(response.data)
                emit(Resource.Success(response.data))
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
                requireNotNull(response.data)
                emit(Resource.Success(response.data))
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
                localDataSource.deletePost(postId)
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(response.message!!, false))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun uploadImage(apiKey: String, postId: Int, bitmap: Bitmap): Flow<Resource<String>> = flow {
        try {
            val requestBody = ByteArrayOutputStream().also {
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
            }
                .toByteArray()
                .toRequestBody("image/jpeg".toMediaTypeOrNull())
            val response = postService.uploadImage(
                apiKey,
                postId,
                MultipartBody.Part.createFormData("image", "${System.currentTimeMillis()}.jpg", requestBody)
            )

            if (!response.error) {
                requireNotNull(response.data)
                emit(Resource.Success(response.data))
            }
        } catch (e: IOException) {
            emit(Resource.Error(e.message!!))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message()))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun removePostImages(apiKey: String, postId: Int, list: String): Flow<Resource<String>> = flow {
        try {
            val response = postService.removeImages(apiKey, postId, list)

            if (!response.error) {
                requireNotNull(response.data)
                emit(Resource.Success(response.data))
            }
        } catch (e: IOException) {
            emit(Resource.Error(e.message!!))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message()))
        }
    }
        .onStart { emit(Resource.Loading()) }

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

    fun clearCache(groupId: Int) {
        localDataSource.deleteAll(groupId)
    }

    companion object {
        @Volatile private var instance: PostRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: PostRepository(PostService.create(), PostDao).also { instance = it }
            }
    }
}