package com.hhp227.application.api

import android.util.Log
import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.model.GroupItem
import com.hhp227.application.util.URLs
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
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
        @Query("status") status: String = "0"
    ): BasicApiResponse<List<GroupItem.Group>>

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
