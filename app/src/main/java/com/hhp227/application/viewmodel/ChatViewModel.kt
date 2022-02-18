package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.ChatRepository
import com.hhp227.application.dto.ChatRoomItem
import com.hhp227.application.dto.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChatViewModel internal constructor(private val repository: ChatRepository) : ViewModel() {
    val state = MutableStateFlow(State())

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "ChatViewModel onCleared")
    }

    private fun fetchChatList() {
        repository.getChatList().onEach { result ->
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
                        error = result.message.toString()
                    )
                }
                is Resource.Loading -> {
                    state.value = state.value.copy(
                        isLoading = true
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    init {
        fetchChatList()
    }

    data class State(
        val chatRooms: List<ChatRoomItem> = emptyList(),
        val isLoading: Boolean = false,
        val error: String = ""
    )
}

class ChatViewModelFactory(
    private val repository: ChatRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(repository) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}