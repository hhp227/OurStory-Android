package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.ChatRepository
import com.hhp227.application.dto.ChatRoomItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChatViewModel : ViewModel() {
    val state = MutableStateFlow(State())

    val repository = ChatRepository()

    private fun getChatList() {
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
        getChatList()
    }

    data class State(
        val chatRooms: List<ChatRoomItem> = emptyList(),
        val isLoading: Boolean = false,
        val error: String = ""
    )
}