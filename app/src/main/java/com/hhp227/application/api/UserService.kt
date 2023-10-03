package com.hhp227.application.api

import android.util.Log
import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.model.User
import com.hhp227.application.util.URLs
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {

    @GET("users/{group_id}")
    suspend fun getUserList(
        @Path("group_id") groupId: Int
    ): BasicApiResponse<List<User>>

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