package com.hhp227.application.viewmodel

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.R
import com.hhp227.application.data.PostRepository
import com.hhp227.application.data.ReplyRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.Resource
import com.hhp227.application.model.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostDetailViewModel internal constructor(
    private val postRepository: PostRepository,
    private val replyRepository: ReplyRepository,
    private val savedStateHandle: SavedStateHandle,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    private lateinit var apiKey: String

    val isScrollToLast get() = savedStateHandle.get<Boolean>("is_bottom") ?: false

    val groupName = savedStateHandle.get<String>("group_name")

    val post = savedStateHandle.get<ListItem.Post>("post") ?: ListItem.Post()

    val user: LiveData<User?> get() = preferenceManager.userFlow.asLiveData()

    val state = MutableLiveData(State(itemList = listOf(post)))

    private fun fetchPost(postId: Int) {
        postRepository.getPost(postId)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { post ->
                            savedStateHandle["post"] = post
                            state.value = state.value?.copy(
                                textError = null,
                                isLoading = false,
                                itemList = state.value!!.itemList + post,
                                isSetResultOK = post.reportCount > MAX_REPORT_COUNT
                            )

                            updatePost(post)
                        }
                        fetchReplyList(postId)
                    }
                    is Resource.Error -> {
                        state.value = state.value?.copy(
                            textError = null,
                            isLoading = false,
                            message = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(textError = null, isLoading = true)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updatePost(newPost: ListItem.Post) {
        post.id = newPost.id
        post.userId = newPost.userId
        post.name = newPost.name
        post.text = newPost.text
        post.status = newPost.status
        post.profileImage = newPost.profileImage
        post.timeStamp = newPost.timeStamp
        post.replyCount = newPost.replyCount
        post.likeCount = newPost.likeCount
        post.reportCount = newPost.reportCount
        post.attachment = newPost.attachment
    }

    private fun fetchReplyList(postId: Int) {
        // TODO offset 추가할것
        replyRepository.getReplyList(apiKey, postId)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value?.copy(
                            textError = null,
                            isLoading = false,
                            itemList = state.value!!.itemList + (result.data ?: emptyList())
                        )
                    }
                    is Resource.Error -> {
                        state.value = state.value?.copy(
                            textError = null,
                            isLoading = false,
                            message = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(textError = null, isLoading = true)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun fetchReply(replyId: Int) {
        if (replyId >= 0) {
            replyRepository.getReply(apiKey, replyId)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            state.value = state.value?.copy(
                                textError = null,
                                isLoading = false,
                                itemList = state.value!!.itemList + (result.data ?: ListItem.Reply()),
                                replyId = -1
                            )
                        }
                        is Resource.Error -> {
                            state.value = state.value?.copy(
                                textError = null,
                                isLoading = false,
                                message = result.message ?: "An unexpected error occured"
                            )
                        }
                        is Resource.Loading -> {
                            state.value = state.value?.copy(
                                textError = null,
                                isLoading = true,
                                replyId = -1
                            )
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
                        state.value = state.value?.copy(
                            textError = null,
                            isLoading = false,
                            isSetResultOK = result.data ?: false
                        )
                    }
                    is Resource.Error -> {
                        state.value = state.value?.copy(
                            textError = null,
                            isLoading = false,
                            message = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(textError = null, isLoading = true)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun insertReply() {
        if (!TextUtils.isEmpty(state.value!!.reply)) {
            replyRepository.addReply(apiKey, post.id, state.value!!.reply)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            state.value = state.value?.copy(
                                textError = null,
                                isLoading = false,
                                replyId = (result.data as? ListItem.Reply)?.id ?: -1
                            )
                        }
                        is Resource.Error -> {
                            state.value = state.value?.copy(
                                textError = null,
                                isLoading = false,
                                message = result.message ?: "An unexpected error occured"
                            )
                        }
                        is Resource.Loading -> {
                            state.value = state.value?.copy(textError = null, isLoading = true)
                        }
                    }
                }
                .launchIn(viewModelScope)
        } else {
            state.value = state.value?.copy(textError = R.string.input_comment)
        }
    }

    /*fun updateReply(reply: ListItem.Reply) {
        val replyList = state.value?.itemList?.toMutableList() ?: mutableListOf()
        val position = replyList.indexOfFirst { (it as? ListItem.Reply)?.id == reply.id }

        if (position > -1) {
            replyList[position] = reply
            state.value = state.value?.copy(textError = null, itemList = replyList)
        }
    }*/

    fun deleteReply(reply: ListItem.Reply) {
        replyRepository.removeReply(apiKey, reply.id)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        val replyList = state.value?.itemList?.toMutableList() ?: mutableListOf()
                        val position = replyList.indexOfFirst { (it as? ListItem.Reply)?.id == reply.id }

                        if (result.data == true) {
                            replyList.removeAt(position)
                            if (position > 0) {
                                state.value = state.value?.copy(
                                    textError = null,
                                    isLoading = false,
                                    itemList = replyList
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        state.value = state.value?.copy(
                            textError = null,
                            isLoading = false,
                            message = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(textError = null, isLoading = true)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun refreshPostList() {
        viewModelScope.launch {
            state.value = State()

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
                        state.value = state.value?.copy(
                            textError = null,
                            isLoading = false,
                            message = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(textError = null, isLoading = true)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleUserBlocking() {
        Log.e("TEST", "userId: ${post.userId}")
    }

    fun setScrollToLast(boolean: Boolean) {
        savedStateHandle["is_bottom"] = boolean
    }

    init {
        viewModelScope.launch {
            preferenceManager.userFlow.collectLatest { user ->
                apiKey = user?.apiKey ?: ""
            }
        }
        fetchReplyList(post.id)
    }

    companion object {
        const val MAX_REPORT_COUNT = 2
    }

    data class State(
        var reply: String = "",
        val textError: Int? = null,
        val isLoading: Boolean = false,
        val itemList: List<ListItem> = emptyList(),
        val replyId: Int = -1,
        val isSetResultOK: Boolean = false,
        val message: String = ""
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