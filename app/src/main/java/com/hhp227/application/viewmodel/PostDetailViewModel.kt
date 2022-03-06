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
import com.hhp227.application.dto.ListItem
import com.hhp227.application.dto.UserItem
import com.hhp227.application.dto.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostDetailViewModel internal constructor(
    private val postRepository: PostRepository,
    private val replyRepository: ReplyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val apiKey: String by lazy { AppController.getInstance().preferenceManager.user!!.apiKey }

    val state = MutableStateFlow(State())

    var post: ListItem.Post

    val groupName = savedStateHandle.get<String>("group_name")

    var isBottom = savedStateHandle.get<Boolean>("is_bottom") ?: false

    var isUpdate = false

    var isAuth: Boolean

    private fun fetchPost(postId: Int) {
        postRepository.getPost(postId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    post = result.data ?: ListItem.Post()
                    state.value = state.value.copy(
                        isLoading = false,
                        itemList = state.value.itemList + post
                    )

                    fetchReplyList(postId)
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

    fun deletePost() {
        postRepository.removePost(apiKey, post.id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        isPostDeleted = result.data ?: false
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

    private fun fetchReplyList(postId: Int) {
        // TODO offset 추가할것
        replyRepository.getReplyList(apiKey, postId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        itemList = state.value.itemList + (result.data ?: emptyList())
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

    private fun fetchReply(replyId: Int) {
        if (replyId >= 0) {
            replyRepository.getReply(apiKey, replyId).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value.copy(
                            isLoading = false,
                            itemList = state.value.itemList + (result.data ?: ListItem.Reply()),
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
            replyRepository.addReply(apiKey, post.id, text).onEach { result ->
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

    fun updateReply(reply: ListItem.Reply) {
        val replyList = state.value.itemList.toMutableList()
        val position = replyList.indexOfFirst { (it as? ListItem.Reply)?.id == reply.id }

        if (position > -1) {
            replyList[position] = reply
            state.value = state.value.copy(itemList = replyList)
        }
    }

    fun deleteReply(reply: ListItem.Reply) {
        replyRepository.removeReply(apiKey, reply.id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val replyList = state.value.itemList.toMutableList()
                    val position = replyList.indexOfFirst { (it as? ListItem.Reply)?.id == reply.id }

                    if (result.data == true) {
                        replyList.removeAt(position)
                        if (position > 0) {
                            state.value = state.value.copy(
                                isLoading = false,
                                itemList = replyList
                            )
                        }
                    }
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

    fun refreshPostList() {
        viewModelScope.launch {
            state.value = State()

            delay(200)
            fetchPost(post.id)
        }
    }

    init {
        post = savedStateHandle.get<ListItem.Post>("post")?.also { post -> fetchPost(post.id) } ?: ListItem.Post()
        isAuth = AppController.getInstance().preferenceManager.user?.id == post.userId
    }

    data class State(
        val isLoading: Boolean = false,
        val itemList: List<ListItem> = emptyList(),
        val replyId: Int = -1,
        val isPostDeleted: Boolean = false,
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