package com.hhp227.application.data

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.api.GroupService
import com.hhp227.application.app.AppController
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.Resource
import com.hhp227.application.util.URLs
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.IOException

class GroupRepository(private val groupService: GroupService) {
    fun getMyGroupList(apiKey: String): LiveData<PagingData<GroupItem>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 5),
            pagingSourceFactory = { GroupGridPagingSource(groupService, apiKey) }
        ).liveData
    }

    fun getNotJoinedGroupList(apiKey: String): LiveData<PagingData<GroupItem>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 5),
            pagingSourceFactory = { GroupListPagingSource(groupService, apiKey, 0) }
        ).liveData
    }

    fun getJoinRequestGroupList(apiKey: String): LiveData<PagingData<GroupItem>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 5),
            pagingSourceFactory = { GroupListPagingSource(groupService, apiKey, 1) }
        ).liveData
    }

    // TODO
    fun requestToJoinOrCancel(apiKey: String, isSignUp: Boolean, joinType: Int, groupId: Int) = callbackFlow<Resource<Boolean>> {
        val stringRequest = object : StringRequest(if (isSignUp) Method.POST else Method.DELETE, if (isSignUp) URLs.URL_GROUP_JOIN_REQUEST else "${URLs.URL_LEAVE_GROUP}/${groupId}", Response.Listener { response ->
            if (!JSONObject(response).getBoolean("error")) {
                trySendBlocking(Resource.Success(true))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = hashMapOf("Authorization" to apiKey)

            override fun getParams() = hashMapOf(
                "group_id" to groupId.toString(),
                "status" to joinType.toString() // join type이 0이면 0 1이면 1
            )
        }

        AppController.getInstance().addToRequestQueue(stringRequest)
        awaitClose { close() }
    }

    fun addGroupImage(apiKey: String, bitMap: Bitmap): Flow<Resource<String>> = flow {
        try {
            val requestBody = ByteArrayOutputStream().also {
                bitMap.compress(Bitmap.CompressFormat.PNG, 80, it)
            }
                .toByteArray()
                .toRequestBody("image/jpeg".toMediaTypeOrNull())
            val response = groupService.uploadImage(
                apiKey,
                MultipartBody.Part.createFormData("image", "${System.currentTimeMillis()}.jpg", requestBody)
            )

            if (!response.error) {
                emit(Resource.Success(response.data!!))
            }
        } catch (e: IOException) {
            emit(Resource.Error(e.message!!))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message()))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun addGroup(apiKey: String, title: String, description: String, joinType: String, image: String?): Flow<Resource<GroupItem.Group>> = flow {
        try {
            val response = groupService.addGroup(apiKey, title, description, joinType, image)

            if (!response.error) {
                emit(Resource.Success(response.data!!))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun removeGroup(apiKey: String, groupId: Int, isAuth: Boolean): Flow<Resource<Boolean>> = flow {
        try {
            val response = if (isAuth) groupService.removeGroup(apiKey, groupId) else groupService.leaveGroup(apiKey, groupId)

            if (!response.error) {
                emit(Resource.Success(true))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    companion object {
        @Volatile private var instance: GroupRepository? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: GroupRepository(GroupService.create()).also {
                instance = it
            }
        }
    }
}