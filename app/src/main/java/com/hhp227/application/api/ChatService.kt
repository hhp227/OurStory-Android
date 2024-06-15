package com.hhp227.application.api

import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.model.ChatItem
import com.hhp227.application.util.InjectorUtils
import retrofit2.http.*

interface ChatService {
    @GET("chat_rooms")
    suspend fun getChatRoomList(): BasicApiResponse<List<ChatItem.ChatRoom>>

    @GET("chat_rooms/{chat_room_id}")
    suspend fun getChatMessageList(
        @Path("chat_room_id") id: Int,
        @Query("offset") offset: Int,
        @Query("load_size") loadSize: Int
    ): BasicApiResponse<ChatItem.MessageInfo>

    @POST("chat_rooms/{chat_room_id}/message")
    @FormUrlEncoded
    suspend fun addChatMessage(
        @Header("Authorization") apiKey: String,
        @Path("chat_room_id") id: Int,
        @Field("message") text: String
    ): BasicApiResponse<ChatItem.Message>

    companion object {
        fun create(): ChatService {
            return InjectorUtils.provideRetrofit()
                .create(ChatService::class.java)
        }
    }
}