package com.hhp227.application.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.ChatRepository
import com.hhp227.application.dto.MessageItem
import kotlinx.coroutines.flow.MutableStateFlow

class ChatMessageViewModel internal constructor(private val repository: ChatRepository, savedStateHandle: SavedStateHandle) : ViewModel() {
    val state = MutableStateFlow(State())

    val chatRoomId: Int = savedStateHandle.get("chat_room_id") ?: -1

    data class State(
        val isLoading: Boolean = false,
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