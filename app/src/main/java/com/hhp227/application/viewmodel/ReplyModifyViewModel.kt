package com.hhp227.application.viewmodel

import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.app.AppController
import com.hhp227.application.data.ReplyRepository
import com.hhp227.application.dto.ReplyItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ReplyModifyViewModel : ViewModel() {
    val state = MutableStateFlow(State())

    val repository = ReplyRepository()

    val apiKey = AppController.getInstance().preferenceManager.user.apiKey

    var position = 0

    lateinit var replyItem: ReplyItem.Reply

    fun updateReply(text: String) {
        if (!TextUtils.isEmpty(text)) {
            repository.setReply(apiKey, replyItem.id, text).onEach { result ->
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
            state.value = State(error = "내용을 입력하세요.")
    }

    data class State(
        val isLoading: Boolean = false,
        val text: String? = null,
        val error: String = ""
    )
}