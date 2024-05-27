package com.hhp227.application.viewmodel

import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.ChatRepository
import com.hhp227.application.model.Resource
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.ChatItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChatMessageViewModel internal constructor(
    private val repository: ChatRepository,
    preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private lateinit var apiKey: String

    private val chatRoomId: Int

    val state = MutableStateFlow(State())

    val userFlow = preferenceManager.userFlow

    private fun fetchChatThread(chatRoomId: Int, offset: Int) {
        repository.getChatMessages(chatRoomId, offset)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value.copy(
                            isLoading = false,
                            listMessages = ((result.data?.messageList ?: emptyList()) + state.value.listMessages).toMutableList(),
                            offset = state.value.offset + (result.data?.messageList?.size ?: 0),
                            hasRequestedMore = true
                        )
                    }
                    is Resource.Error -> {
                        state.value = state.value.copy(
                            isLoading = false,
                            hasRequestedMore = false,
                            message = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value.copy(
                            isLoading = true,
                            hasRequestedMore = false)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage(text: String) {
        if (!TextUtils.isEmpty(text)) {
            repository.addChatMessage(apiKey, chatRoomId, text)
                .onEach { result ->
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
                                message = result.message ?: "An unexpected error occured"
                            )
                        }
                        is Resource.Loading -> {
                            state.value = State(isLoading = true)
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun fetchNextPage() {
        if (state.value.hasRequestedMore) {
            fetchChatThread(chatRoomId, state.value.offset)
        }
    }

    fun addItem(item: ChatItem.Message) {
        state.value = state.value.copy(listMessages = state.value.listMessages + item)
    }

    init {
        chatRoomId = savedStateHandle.get<Int>("chat_room_id")?.also { chatRoomId -> fetchChatThread(chatRoomId, state.value.offset) } ?: -1

        viewModelScope.launch {
            userFlow.collectLatest { user ->
                apiKey = user?.apiKey ?: ""
            }
        }
    }

    data class State(
        val isLoading: Boolean = false,
        val messageId: Int = -1,
        val listMessages: List<ChatItem.Message> = mutableListOf(),
        var offset: Int = 0,
        var previousMessageCnt: Int = 0,
        var hasRequestedMore: Boolean = false,
        val message: String = ""
    )
}

class ChatMessageViewModelFactory(
    private val repository: ChatRepository,
    private val preferenceManager: PreferenceManager,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(ChatMessageViewModel::class.java)) {
            return ChatMessageViewModel(repository, preferenceManager, handle) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}