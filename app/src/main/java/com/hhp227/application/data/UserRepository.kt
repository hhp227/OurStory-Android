package com.hhp227.application.data

import android.graphics.Bitmap
import com.hhp227.application.api.AuthService
import com.hhp227.application.api.UserService
import com.hhp227.application.model.Resource
import com.hhp227.application.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.IOException

class UserRepository(
    private val authService: AuthService,
    private val userService: UserService
) {
    fun login(email: String, password: String): Flow<Resource<out User>> = flow {
        try {
            val response = authService.login(email, password)

            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage, null))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun register(name: String, email: String, password: String): Flow<Resource<out Unit>> = flow {
        try {
            val response = authService.register(name, email, password)

            if (!response.error) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(response.message ?: "error", null))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage, null))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun getUserList(groupId: Int): Flow<Resource<out List<User>>> = flow {
        try {
            val response = userService.getUserList(groupId)

            if (!response.error) {
                requireNotNull(response.data)
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Error(response.message!!, null))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage, null))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun addProfileImage(apiKey: String, bitmap: Bitmap): Flow<Resource<String>> = flow {
        try {
            val requestBody = ByteArrayOutputStream().also {
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
            }
                .toByteArray()
                .toRequestBody("image/jpeg".toMediaTypeOrNull())
            val response = userService.uploadImage(
                apiKey,
                MultipartBody.Part.createFormData("profile_img", "${System.currentTimeMillis()}.jpg", requestBody)
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

    fun setUserProfile(apiKey: String, imageUrl: String?): Flow<Resource<String>> = flow {
        try {
            val response = userService.setProfile(apiKey, imageUrl, 1)

            if (!response.error) {
                emit(Resource.Success(imageUrl ?: "null"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    // TODO
    fun removeUser() {

    }

    fun isFriend(apiKey: String, friendId: Int): Flow<Resource<Int>> = flow {
        try {
            val response = userService.isFriend(apiKey, friendId)

            if (!response.error) {
                emit(Resource.Success(response.data?.get("cnt") ?: 0))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun getFriendList(apiKey: String, offset: Int): Flow<Resource<List<User>>> = flow {
        try {
            val response = userService.getFriendList(apiKey, offset)

            if (!response.error) {
                requireNotNull(response.data)
                emit(Resource.Success(response.data))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun toggleFriend(apiKey: String, friendId: Int): Flow<Resource<String>> = flow {
        try {
            val response = userService.toggleFriend(apiKey, friendId)

            if (!response.error) {
                requireNotNull(response.data)
                emit(Resource.Success(response.data))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    companion object {
        @Volatile private var instance: UserRepository? = null

        fun getInstance(authService: AuthService, userService: UserService) =
            instance ?: synchronized(this) {
                instance ?: UserRepository(authService, userService).also { instance = it }
            }
    }
}