package com.hhp227.application.viewmodel

import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.app.AppController
import com.hhp227.application.data.ChatRepository
import com.hhp227.application.dto.MessageItem
import com.hhp227.application.dto.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChatMessageViewModel internal constructor(private val repository: ChatRepository, savedStateHandle: SavedStateHandle) : ViewModel() {
    val state = MutableStateFlow(State())

    val chatRoomId: Int

    val apiKey = AppController.getInstance().preferenceManager.user.apiKey

    private fun fetchChatThread(chatRoomId: Int, offset: Int) {
        repository.getChatMessages(chatRoomId, offset).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        listMessages = (state.value.listMessages + (result.data ?: emptyList())).toMutableList(),
                        offset = state.value.offset + (result.data?.size ?: 0)
                    )
                }
                is Resource.Error -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        error = result.message ?: "An unexpected error occured"
                    )
                }
                is Resource.Loading -> {
                    state.value = state.value.copy(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun sendMessage(text: String) {
        if (!TextUtils.isEmpty(text)) {
            repository.addChatMessage(apiKey, chatRoomId, text).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value.copy(
                            isLoading = false,
                            messageId = result.data?.id ?: -1,
                            listMessages = (state.value.listMessages + result.data!!).toMutableList()
                        )
                    }
                    is Resource.Error -> {
                        state.value = state.value.copy(
                            isLoading = false,
                            error = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = State(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun fetchNextPage() {

    }

    init {
        chatRoomId = savedStateHandle.get<Int>("chat_room_id")?.also { chatRoomId -> fetchChatThread(chatRoomId, state.value.offset) } ?: -1
    }

    data class State(
        val isLoading: Boolean = false,
        val messageId: Int = -1,
        val listMessages: MutableList<MessageItem> = mutableListOf(),
        var offset: Int = 0,
        var previousMessageCnt: Int = 0,
        var hasRequestedMore: Boolean = false,
        val error: String = ""
    )
}

class ChatMessageViewModelFactory(
    private val repository: ChatRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(ChatMessageViewModel::class.java)) {
            return ChatMessageViewModel(repository, handle) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}