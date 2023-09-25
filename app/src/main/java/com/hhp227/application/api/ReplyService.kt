package com.hhp227.application.api

import android.util.Log
import com.hhp227.application.model.AddReplyResponse
import com.hhp227.application.model.BasicApiResponse
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
    // Api를 전반적으로 손봐야할 필요가 있다. { error: false, reply: { ....} } 이런식으로
    /*@GET("replys/post/{reply_id}")
    fun getReply(
        @Header("Authorization") apiKey: String,
        @Path("reply_id") replyId: String
    ): ListItem.Reply*/

    @POST("replys/{post_id}")
    @FormUrlEncoded
    suspend fun addReply(
        @Header("Authorization") apiKey: String,
        @Path("post_id") portId: Int,
        @Field("reply") text: String
    ): AddReplyResponse

    @PUT("replys/post/{reply_id}")
    @FormUrlEncoded
    suspend fun setReply(
        @Header("Authorization") apiKey: String,
        @Path("reply_id") replyId: String,
        @Field("reply") text: String,
        @Field("status") status: String = "0"
    ): BasicApiResponse<String>

    @DELETE("replys/post/{reply_id}")
    suspend fun removeReply(
        @Header("Authorization") apiKey: String,
        @Path("reply_id") replyId: String
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