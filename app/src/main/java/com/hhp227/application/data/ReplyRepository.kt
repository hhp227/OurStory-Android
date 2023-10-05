package com.hhp227.application.data

import com.hhp227.application.api.ReplyService
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

class ReplyRepository(private val replyService: ReplyService) {
    fun getReplyList(apiKey: String, postId: Int): Flow<Resource<out List<ListItem>>> = flow {
        try {
            val response = replyService.getReplyList(apiKey, postId)

            if (!response.error) {
                emit(Resource.Success(response.data!!))
            } else {
                emit(Resource.Error(response.message!!, null))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage, null))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun getReply(apiKey: String, replyId: Int): Flow<Resource<out ListItem>> = flow {
        try {
            val response = replyService.getReply(apiKey, replyId)

            if (!response.error) {
                emit(Resource.Success(response.data!!))
            } else {
                emit(Resource.Error(response.message!!, null))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage, null))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun addReply(apiKey: String, postId: Int, text: String): Flow<Resource<out ListItem>> = flow {
        try {
            val response = replyService.addReply(apiKey, postId, text)

            if (!response.error) {
                emit(Resource.Success(response.data!!))
            } else {
                emit(Resource.Error(response.message!!, null))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage, null))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun setReply(apiKey: String, replyId: Int, text: String): Flow<Resource<out String?>> = flow {
        try {
            val response = replyService.setReply(apiKey, replyId, text)

            if (!response.error) {
                emit(Resource.Success(text))
            } else {
                emit(Resource.Error(response.message!!, null))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage, null))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun removeReply(apiKey: String, replyId: Int): Flow<Resource<out Boolean>> = flow {
        try {
            val response = replyService.removeReply(apiKey, replyId)

            if (!response.error) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(response.message!!, null))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage, null))
        }
    }
        .onStart { emit(Resource.Loading()) }

    companion object {
        @Volatile private var instance: ReplyRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: ReplyRepository(ReplyService.create()).also { instance = it }
            }
    }
}