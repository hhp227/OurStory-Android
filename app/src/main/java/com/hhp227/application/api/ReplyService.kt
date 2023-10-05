package com.hhp227.application.api

import android.util.Log
import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.model.ListItem
import com.hhp227.application.util.URLs
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.*

interface ReplyService {
    @GET("replys/{post_id}")
    suspend fun getReplyList(
        @Header("Authorization") apiKey: String,
        @Path("post_id") postId: Int
    ): BasicApiResponse<List<ListItem.Reply>>

    @GET("replys/post/{reply_id}")
    suspend fun getReply(
        @Header("Authorization") apiKey: String,
        @Path("reply_id") replyId: Int
    ): BasicApiResponse<ListItem.Reply>

    @POST("replys/{post_id}")
    @FormUrlEncoded
    suspend fun addReply(
        @Header("Authorization") apiKey: String,
        @Path("post_id") portId: Int,
        @Field("reply") text: String
    ): BasicApiResponse<ListItem.Reply>

    @PUT("replys/post/{reply_id}")
    @FormUrlEncoded
    suspend fun setReply(
        @Header("Authorization") apiKey: String,
        @Path("reply_id") replyId: Int,
        @Field("reply") text: String,
        @Field("status") status: String = "0"
    ): BasicApiResponse<String>

    @DELETE("replys/post/{reply_id}")
    suspend fun removeReply(
        @Header("Authorization") apiKey: String,
        @Path("reply_id") replyId: Int
    ): BasicApiResponse<Boolean>

    companion object {
        private val Json = Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        fun create(): ReplyService {
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
                .create(ReplyService::class.java)
        }
    }
}