package com.hhp227.application.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hhp227.application.api.ChatService
import com.hhp227.application.model.ChatItem
import com.hhp227.application.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

class ChatRepository(
    private val chatService: ChatService,
    private val localDataSource: ChatDao
) {
    private lateinit var pagingSource: ChatMessagePagingSource

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

    fun getChatMessageList(chatRoomId: Int): Flow<PagingData<ChatItem.Message>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 10),
            pagingSourceFactory = { ChatMessagePagingSource(chatService, localDataSource, chatRoomId).also { pagingSource = it } }
        ).flow
    }

    fun addChatMessage(apiKey: String, chatRoomId: Int, textMessage: String): Flow<Resource<ChatItem.Message>> = flow {
        try {
            val response = chatService.addChatMessage(apiKey, chatRoomId, textMessage)

            if (!response.error) {
                localDataSource.insert(chatRoomId, response.data!!)
                emit(Resource.Success(response.data))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message!!))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun invalidateChatMessageList(chatRoomId: Int) {
        localDataSource.deleteAll(chatRoomId)
        pagingSource.invalidate()
    }

    companion object {
        @Volatile private var instance: ChatRepository? = null

        fun getInstance(chatService: ChatService, chatDao: ChatDao) = instance ?: synchronized(this) {
            instance ?: ChatRepository(chatService, chatDao).also {
                instance = it
            }
        }
    }
}