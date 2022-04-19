package com.hhp227.application.viewmodel

import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.R
import com.hhp227.application.data.ReplyRepository
import com.hhp227.application.dto.ListItem
import com.hhp227.application.dto.Resource
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UpdateReplyViewModel internal constructor(private val repository: ReplyRepository, preferenceManager: PreferenceManager, savedStateHandle: SavedStateHandle) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableStateFlow(State())

    val reply: ListItem.Reply = savedStateHandle.get("reply") ?: ListItem.Reply()

    fun updateReply(text: String) {
        if (!TextUtils.isEmpty(text)) {
            repository.setReply(apiKey, reply.id, text).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = State(
                            isLoading = false,
                            text = result.data
                        )
                    }
                    is Resource.Error -> {
                        state.value = State(
                            isLoading = false,
                            error = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = State(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        } else
            state.value = State(textFieldState = TextFieldState(R.string.input_content))
    }

    init {
        viewModelScope.launch {
            preferenceManager.userFlow.collectLatest { user ->
                apiKey = user?.apiKey ?: ""
            }
        }
    }

    data class State(
        val isLoading: Boolean = false,
        val text: String? = null,
        val textFieldState: TextFieldState? = null,
        val error: String = ""
    )

    data class TextFieldState(
        val textError: Int? = null
    )
}

class UpdateReplyViewModelFactory(
    private val repository: ReplyRepository,
    private val preferenceManager: PreferenceManager,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(UpdateReplyViewModel::class.java)) {
            return UpdateReplyViewModel(repository, preferenceManager, handle) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}