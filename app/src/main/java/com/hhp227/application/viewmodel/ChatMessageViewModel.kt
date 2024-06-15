package com.hhp227.application.viewmodel

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.*
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.ChatRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.ChatItem
import com.hhp227.application.model.Resource
import com.hhp227.application.model.User
import kotlinx.coroutines.flow.catch
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

    val state = MutableLiveData(State())

    private fun fetchChatThread(chatRoomId: Int) {
        repository.getChatMessageList(chatRoomId)
            .catch { state.value = state.value?.copy(message = it.message) }
            .cachedIn(viewModelScope)
            .onEach(::setPagingData)
            .launchIn(viewModelScope)
    }

    private fun setPagingData(pagingData: PagingData<ChatItem.Message>) {
        state.value = state.value?.copy(pagingData = pagingData)
    }

    fun sendMessage() {
        if (!TextUtils.isEmpty(state.value?.text?.value)) {
            repository.addChatMessage(apiKey, chatRoomId, state.value?.text?.value!!)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            addChatMessage()
                        }
                        is Resource.Error -> {
                            state.value = state.value?.copy(
                                isLoading = false,
                                message = result.message ?: "An unexpected error occured"
                            )
                        }
                        is Resource.Loading -> {
                            state.value = state.value?.copy(
                                text = MutableLiveData(""),
                                isLoading = true
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun addChatMessage() {
        repository.invalidateChatMessageList(chatRoomId)
    }

    init {
        chatRoomId = savedStateHandle.get<Int>("chat_room_id")?.also { chatRoomId ->
            fetchChatThread(chatRoomId)
        } ?: -1

        viewModelScope.launch {
            preferenceManager.userFlow.collectLatest { user ->
                apiKey = user?.apiKey ?: ""
                state.value = state.value?.copy(user = user)
            }
        }
    }

    data class State(
        var text: MutableLiveData<String> = MutableLiveData(""),
        val isLoading: Boolean = false,
        val pagingData: PagingData<ChatItem.Message>? = PagingData.empty(),
        val user: User? = null,
        val message: String? = ""
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