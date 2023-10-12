package com.hhp227.application.api

import android.util.Log
import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.model.User
import com.hhp227.application.util.URLs
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {

    @GET("users/{group_id}")
    suspend fun getUserList(
        @Path("group_id") groupId: Int
    ): BasicApiResponse<List<User>>

    @Multipart
    @POST("profile_img")
    suspend fun uploadImage(
        @Header("Authorization") apiKey: String,
        @Part image: MultipartBody.Part
    ): BasicApiResponse<String>

    @PUT("profile")
    @FormUrlEncoded
    suspend fun setProfile(
        @Header("Authorization") apiKey: String,
        @Field("profile_img") imageUrl: String?,
        @Field("status") status: Int
    ): BasicApiResponse<Unit>

    @GET("friends")
    suspend fun getFriendList(
        @Header("Authorization") apiKey: String,
        @Query("offset") offset: Int
    ): BasicApiResponse<List<User>>

    @GET("friend/{user_id}")
    suspend fun isFriend(
        @Header("Authorization") apiKey: String,
        @Path("user_id") userId: Int
    ): BasicApiResponse<Map<String, Int>>

    @GET("toggle_friend/{user_id}")
    suspend fun toggleFriend(
        @Header("Authorization") apiKey: String,
        @Path("user_id") userId: Int
    ): BasicApiResponse<String>

    companion object {
        private val Json = Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        fun create(): UserService {
            val logger = HttpLoggingInterceptor { Log.d("API", it) }
            logger.level = HttpLoggingInterceptor.Level.BASIC
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()
            return Retrofit.Builder()
                .baseUrl(URLs.BASE_URL.plus("/").toHttpUrlOrNull()!!)
                .client(client)
                .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(UserService::class.java)
        }
    }
}