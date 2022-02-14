package com.hhp227.application.viewmodel

import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.app.AppController
import com.hhp227.application.data.PostRepository
import com.hhp227.application.data.ReplyRepository
import com.hhp227.application.dto.PostItem
import com.hhp227.application.dto.ReplyItem
import com.hhp227.application.dto.UserItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PostDetailViewModel internal constructor(
    private val postRepository: PostRepository,
    private val replyRepository: ReplyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val state = MutableStateFlow(State())

    val itemList: MutableList<ReplyItem> by lazy { arrayListOf() }

    val user: UserItem by lazy { AppController.getInstance().preferenceManager.user }

    val post: PostItem.Post = savedStateHandle.get("post") ?: PostItem.Post()

    val groupName = savedStateHandle.get<String>("group_name")

    var isBottom = savedStateHandle.get<Boolean>("is_bottom") ?: false

    var isUpdate = false

    private fun fetchReply(replyId: Int) {
        if (replyId >= 0) {
            replyRepository.getReply(user.apiKey, replyId).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value.copy(
                            isLoading = false,
                            itemList = state.value.itemList + (result.data ?: ReplyItem.Reply()),
                            replyId = -1
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
    }

    fun insertReply(text: String) {
        if (!TextUtils.isEmpty(text)) {
            replyRepository.addReply(user.apiKey, post.id, text).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        val replyId = result.data ?: -1
                        state.value = state.value.copy(
                            isLoading = false,
                            replyId = replyId
                        )

                        fetchReply(replyId)
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
        } else {
            state.value = state.value.copy(
                isLoading = false,
                error = "text is empty"
            )
        }
    }

    data class State(
        val isLoading: Boolean = false,
        val itemList: List<ReplyItem> = emptyList(),
        val replyId: Int = -1,
        val error: String = ""
    )
}

class PostDetailViewModelFactory(
    private val postRepository: PostRepository,
    private val replyRepository: ReplyRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(PostDetailViewModel::class.java)) {
            return PostDetailViewModel(postRepository, replyRepository, handle) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}