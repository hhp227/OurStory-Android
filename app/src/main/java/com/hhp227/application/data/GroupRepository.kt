package com.hhp227.application.data

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.hhp227.application.api.GroupService
import com.hhp227.application.model.GroupItem
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

    fun requestToJoinOrCancel(apiKey: String, isSignUp: Boolean, joinType: Int, groupId: Int): Flow<Resource<Boolean>> = flow {
        try {
            val response = if (isSignUp) groupService.requestJoin(apiKey, groupId, joinType) else groupService.leaveGroup(apiKey, groupId)

            if (!response.error) {
                emit(Resource.Success(true))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun addGroupImage(apiKey: String, bitmap: Bitmap): Flow<Resource<String>> = flow {
        try {
            // 아래 과정을 workmanager 로 하는게 좋다.
            val requestBody = ByteArrayOutputStream().also {
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
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