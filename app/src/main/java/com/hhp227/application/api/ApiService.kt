package com.hhp227.application.api

import android.util.Log
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.GetPostListResponse
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    fun request(endpoint: String) {

    }

    @GET("posts")
    suspend fun getPostList(
        @Query("group_id") groupId: Int,
        @Query("offset") offset: Int
    ): GetPostListResponse

    companion object {
        fun create(): ApiService {
            val logger = HttpLoggingInterceptor { Log.d("API", it) }
            logger.level = HttpLoggingInterceptor.Level.BASIC
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()
            return Retrofit.Builder()
                .baseUrl(URLs.BASE_URL.plus("/").toHttpUrlOrNull()!!)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}