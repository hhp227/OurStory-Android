package com.hhp227.application.viewmodel

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.R
import com.hhp227.application.data.ReplyRepository
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.Resource
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UpdateReplyViewModel internal constructor(
    private val repository: ReplyRepository,
    preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private lateinit var apiKey: String

    private val reply: ListItem.Reply = savedStateHandle["reply"] ?: ListItem.Reply()

    val state = MutableLiveData(State(text = reply.reply))

    fun updateReply() {
        if (!TextUtils.isEmpty(state.value!!.text)) {
            repository.setReply(apiKey, reply.id, state.value!!.text!!)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            reply.reply = result.data.toString()
                            state.value = State(
                                text = state.value?.text,
                                textError = null,
                                isLoading = false,
                                isSuccess = state.value!!.text == result.data
                            )

                            Log.e("TEST", "Success updateReply: ${result}")
                        }
                        is Resource.Error -> {
                            state.value = State(
                                text = state.value?.text,
                                textError = null,
                                isLoading = false,
                                isSuccess = false,
                                message = result.message ?: "An unexpected error occured"
                            )
                        }
                        is Resource.Loading -> {
                            state.value = State(
                                text = state.value?.text,
                                isLoading = true
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
        } else {
            state.value = State(textError = R.string.input_content)
        }
    }

    init {
        viewModelScope.launch {
            preferenceManager.getUserFlow()
                .collectLatest { user ->
                    apiKey = user?.apiKey ?: ""
                }
        }
    }

    data class State(
        var text: String? = null,
        val textError: Int? = null,
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val message: String = ""
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