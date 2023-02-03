package com.hhp227.application.api

import android.util.Log
import com.hhp227.application.util.URLs
import com.hhp227.application.model.GetPostListResponse
import com.hhp227.application.model.TogglePostLikeResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface PostService {
    @GET("posts")
    suspend fun getPostList(
        @Query("group_id") groupId: Int,
        @Query("page") page: Int,
        @Query("load_size") loadSize: Int
    ): GetPostListResponse

    @GET("like/{id}")
    suspend fun togglePostLike(
        @Header("Authorization") apiKey: String,
        @Query("id") postId: Int
    ): TogglePostLikeResponse

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