package com.hhp227.application.data

import com.hhp227.application.api.ChatService
import com.hhp227.application.model.ChatItem
import com.hhp227.application.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

class ChatRepository(private val chatService: ChatService) {
    fun getChatRoomList(): Flow<Resource<List<ChatItem.ChatRoom>>> = flow {
        try {
            val response = chatService.getChatRoomList()

            if (!response.error) {
                emit(Resource.Success(response.data!!))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun getChatMessages(chatRoomId: Int, offset: Int): Flow<Resource<ChatItem.MessageInfo>> = flow {
        try {
            val response = chatService.getChatMessageList(chatRoomId, offset)

            if (!response.error) {
                emit(Resource.Success(response.data!!))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun addChatMessage(apiKey: String, chatRoomId: Int, textMessage: String): Flow<Resource<ChatItem.Message>> = flow {
        try {
            val response = chatService.addChatMessage(apiKey, chatRoomId, textMessage)

            if (!response.error) {
                emit(Resource.Success(response.data!!))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    companion object {
        @Volatile private var instance: ChatRepository? = null

        fun getInstance(chatService: ChatService) = instance ?: synchronized(this) {
            instance ?: ChatRepository(chatService).also {
                instance = it
            }
        }
    }
}