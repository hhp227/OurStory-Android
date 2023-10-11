package com.hhp227.application.api

import android.util.Log
import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.model.ChatItem
import com.hhp227.application.util.URLs
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatService {
    @GET("chat_rooms")
    suspend fun getChatRoomList(): BasicApiResponse<List<ChatItem.ChatRoom>>

    @GET("chat_rooms/{chat_room_id}")
    suspend fun getChatMessageList(
        @Path("chat_room_id") id: Int,
        @Query("offset") offset: Int
    ): BasicApiResponse<ChatItem.MessageInfo>

    @POST("chat_rooms/{chat_room_id}/message")
    @FormUrlEncoded
    suspend fun addChatMessage(
        @Header("Authorization") apiKey: String,
        @Path("chat_room_id") id: Int,
        @Field("message") text: String
    ): BasicApiResponse<ChatItem.Message>

    companion object {
        private val Json = Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        fun create(): ChatService {
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
                .create(ChatService::class.java)
        }
    }
}