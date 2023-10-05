package com.hhp227.application.api

import android.util.Log
import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.model.ListItem
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
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PostService {
    @GET("posts")
    suspend fun getPostList(
        @Query("group_id") groupId: Int,
        @Query("page") page: Int,
        @Query("load_size") loadSize: Int
    ): BasicApiResponse<List<ListItem.Post>>

    @GET("posts_image")
    suspend fun getPostListWithImage(
        @Query("group_id") groupId: Int,
        @Query("page") page: Int,
        @Query("load_size") loadSize: Int
    ): BasicApiResponse<List<ListItem.Post>>

    @GET("posts/")
    suspend fun getUserPostList(
        @Header("Authorization") apiKey: String,
        @Query("page") page: Int,
        @Query("load_size") loadSize: Int
    ): BasicApiResponse<List<ListItem.Post>>

    @GET("post/{post_id}")
    suspend fun getPost(
        @Path("post_id") postId: Int
    ): BasicApiResponse<ListItem.Post>

    @POST("post")
    @FormUrlEncoded
    suspend fun addPost(
        @Header("Authorization") apiKey: String,
        @Field("text") text: String,
        @Field("group_id") groupId: Int
    ): BasicApiResponse<Int>

    @PUT("post/{post_id}")
    @FormUrlEncoded
    suspend fun setPost(
        @Header("Authorization") apiKey: String,
        @Path("post_id") postId: Int,
        @Field("text") text: String,
        @Field("status") status: Int = 0
    ): BasicApiResponse<Int>

    @DELETE("post/{post_id}")
    suspend fun removePost(
        @Header("Authorization") apiKey: String,
        @Path("post_id") postId: Int
    ): BasicApiResponse<Unit>

    @Multipart
    @POST("image")
    suspend fun uploadImage(
        @Header("Authorization") apiKey: String,
        @Part("post_id") postId: Int,
        @Part image: MultipartBody.Part
    ): BasicApiResponse<String>

    @POST("images")
    @FormUrlEncoded
    suspend fun removeImages(
        @Header("Authorization") apiKey: String,
        @Field("post_id") postId: Int,
        @Field("ids") imageIds: String
    ): BasicApiResponse<String>

    @GET("like/{post_id}")
    suspend fun togglePostLike(
        @Header("Authorization") apiKey: String,
        @Path("post_id") postId: Int
    ): BasicApiResponse<String>

    @GET("report/{post_id}")
    suspend fun toggleReport(
        @Header("Authorization") apiKey: String,
        @Path("post_id") postId: Int
    ): BasicApiResponse<String>

    companion object {
        private val Json = Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        fun create(): PostService {
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
                .create(PostService::class.java)
        }
    }
}