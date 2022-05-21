package com.hhp227.application.viewmodel

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.R
import com.hhp227.application.data.PostRepository
import com.hhp227.application.data.ReplyRepository
import com.hhp227.application.dto.ListItem
import com.hhp227.application.dto.Resource
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostDetailViewModel internal constructor(
    private val postRepository: PostRepository,
    private val replyRepository: ReplyRepository,
    private val savedStateHandle: SavedStateHandle,
    preferenceManager: PreferenceManager
) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableStateFlow(State())

    val textFormState = MutableStateFlow(TextFormState())

    val userFlow = preferenceManager.userFlow

    val isScrollToLastFlow get() = savedStateHandle.getLiveData<Boolean>("is_bottom").asFlow()

    val groupName = savedStateHandle.get<String>("group_name")

    var isAuth = false

    var post: ListItem.Post private set

    var isUpdate = false

    private fun fetchPost(postId: Int) {
        postRepository.getPost(postId)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        post = result.data ?: ListItem.Post()
                        state.value = state.value.copy(
                            isLoading = false,
                            itemList = state.value.itemList + post,
                            isSetResultOK = post.reportCount > MAX_REPORT_COUNT
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
            }
            .launchIn(viewModelScope)
    }

    private fun fetchReplyList(postId: Int) {
        // TODO offset 추가할것
        replyRepository.getReplyList(apiKey, postId)
            .onEach { result ->
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
            }
            .launchIn(viewModelScope)
    }

    private fun fetchReply(replyId: Int) {
        if (replyId >= 0) {
            replyRepository.getReply(apiKey, replyId)
                .onEach { result ->
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
                }
                .launchIn(viewModelScope)
        }
    }

    fun deletePost() {
        postRepository.removePost(apiKey, post.id)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value.copy(
                            isLoading = false,
                            isSetResultOK = result.data ?: false
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
            }
            .launchIn(viewModelScope)
    }

    fun insertReply(text: String) {
        if (!TextUtils.isEmpty(text)) {
            replyRepository.addReply(apiKey, post.id, text)
                .onEach { result ->
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
                }
                .launchIn(viewModelScope)
        } else {
            textFormState.value = textFormState.value.copy(textError = R.string.input_comment)
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
        replyRepository.removeReply(apiKey, reply.id)
            .onEach { result ->
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
            }
            .launchIn(viewModelScope)
    }

    fun refreshPostList() {
        viewModelScope.launch {
            state.value = State()

            delay(200)
            fetchPost(post.id)
        }
    }

    fun togglePostReport() {
        postRepository.toggleReport(apiKey, post.id)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        refreshPostList()
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
            }
            .launchIn(viewModelScope)
    }

    fun toggleUserBlocking() {
        Log.e("TEST", "userId: ${post.userId}")
    }

    fun isAuth(userId: Int) {

    }

    fun setScrollToLast(boolean: Boolean) {
        savedStateHandle.set("is_bottom", boolean)
    }

    init {
        post = savedStateHandle.get<ListItem.Post>("post")?.also { post -> fetchPost(post.id) } ?: ListItem.Post()

        viewModelScope.launch {
            userFlow.collectLatest { user ->
                apiKey = user?.apiKey ?: ""
                isAuth = user?.id == post.userId
            }
        }
    }

    companion object {
        const val MAX_REPORT_COUNT = 2
    }

    data class State(
        val isLoading: Boolean = false,
        val itemList: List<ListItem> = emptyList(),
        val replyId: Int = -1,
        val isSetResultOK: Boolean = false,
        val error: String = ""
    )

    data class TextFormState(
        val textError: Int? = null
    )
}

class PostDetailViewModelFactory(
    private val postRepository: PostRepository,
    private val replyRepository: ReplyRepository,
    private val preferenceManager: PreferenceManager,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(PostDetailViewModel::class.java)) {
            return PostDetailViewModel(postRepository, replyRepository, handle, preferenceManager) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}