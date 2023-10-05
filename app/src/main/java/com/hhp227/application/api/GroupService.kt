package com.hhp227.application.api

import android.util.Log
import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.model.GroupItem
import com.hhp227.application.util.URLs
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface GroupService {
    @GET("groups")
    suspend fun getNotJoinedGroupList(
        @Header("Authorization") apiKey: String,
        @Query("page") page: Int,
        @Query("load_size") loadSize: Int
    ): BasicApiResponse<List<GroupItem.Group>>

    @GET("user_groups")
    suspend fun getMyGroupList(
        @Header("Authorization") apiKey: String,
        @Query("page") page: Int,
        @Query("load_size") loadSize: Int,
        @Query("status") status: Int = 0
    ): BasicApiResponse<List<GroupItem.Group>>

    @POST("group_join")
    @FormUrlEncoded
    suspend fun requestJoin(
        @Header("Authorization") apiKey: String,
        @Field("group_id") groupId: Int,
        @Field("status") status: Int
    ): BasicApiResponse<Boolean>

    @POST("group")
    @FormUrlEncoded
    suspend fun addGroup(
        @Header("Authorization") apiKey: String,
        @Field("name") name: String,
        @Field("description") description: String,
        @Field("join_type") joinType: String,
        @Field("image") image: String?
    ): BasicApiResponse<GroupItem.Group>

    @DELETE("group/{group_id}")
    suspend fun removeGroup(
        @Header("Authorization") apiKey: String,
        @Path("group_id") groupId: Int
    ): BasicApiResponse<Unit>

    @DELETE("leave_group/{group_id}")
    suspend fun leaveGroup(
        @Header("Authorization") apiKey: String,
        @Path("group_id") groupId: Int
    ): BasicApiResponse<Unit>

    @Multipart
    @POST("group_image")
    suspend fun uploadImage(
        @Header("Authorization") apiKey: String,
        @Part image: MultipartBody.Part
    ): BasicApiResponse<String>

    companion object {
        private val Json = Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        fun create(): GroupService {
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
                .create(GroupService::class.java)
        }
    }
}