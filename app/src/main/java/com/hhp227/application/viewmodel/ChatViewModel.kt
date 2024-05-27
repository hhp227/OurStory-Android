package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.ChatRepository
import com.hhp227.application.fcm.FcmTopicSubscriber
import com.hhp227.application.model.ChatItem
import com.hhp227.application.model.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChatViewModel internal constructor(
    private val repository: ChatRepository,
    private val fcmTopicSubscriber: FcmTopicSubscriber
) : ViewModel() {
    val state = MutableStateFlow(State())

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "ChatViewModel onCleared")
    }

    private fun fetchChatList() {
        repository.getChatRoomList()
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value.copy(
                            chatRooms = result.data ?: emptyList(),
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        state.value = state.value.copy(
                            isLoading = false,
                            message = result.message.toString()
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value.copy(
                            isLoading = true
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun subscribeToTopic(chatRooms: List<ChatItem.ChatRoom>) {
        for (chatRoom in chatRooms) {
            fcmTopicSubscriber.subscribeToTopic("topic_${chatRoom.id}")
        }
    }

    init {
        fetchChatList()
    }

    data class State(
        val chatRooms: List<ChatItem.ChatRoom> = emptyList(),
        val isLoading: Boolean = false,
        val message: String = ""
    )
}

class ChatViewModelFactory(
    private val repository: ChatRepository,
    private val topicSubscriber: FcmTopicSubscriber
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(repository, topicSubscriber) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}