package com.hhp227.application.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.ChatRepository
import com.hhp227.application.dto.ChatRoomItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChatListViewModel : ViewModel() {
    val state = MutableLiveData(State())

    val repository = ChatRepository()

    private fun getChatList() {
        repository.getChatList().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.postValue(
                        state.value?.copy(
                            chatRooms = result.data ?: emptyList(),
                            isLoading = false
                        )
                    )
                }
                is Resource.Error -> {
                    state.postValue(
                        state.value?.copy(
                            isLoading = false,
                            error = result.message.toString()
                        )
                    )
                }
                is Resource.Loading -> {
                    state.postValue(
                        state.value?.copy(
                            isLoading = true
                        )
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