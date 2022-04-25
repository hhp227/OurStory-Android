package com.hhp227.application.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.PostRepository
import com.hhp227.application.dto.ListItem
import com.hhp227.application.dto.Resource
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostViewModel internal constructor(private val repository: PostRepository, preferenceManager: PreferenceManager, savedStateHandle: SavedStateHandle): ViewModel() {
    private lateinit var apiKey: String

    private val groupId: Int

    val state = MutableStateFlow(State())

    val userFlow = preferenceManager.userFlow

    var groupName: String? = null

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "Tab1ViewModel onCleared")
    }

    fun fetchPostList(id: Int = groupId, offset: Int) {
        repository.getPostList(id, offset).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        itemList = state.value.itemList + (result.data ?: emptyList()),
                        offset = state.value.offset + (result.data?.size ?: 0),
                        hasRequestedMore = false
                    )
                }
                is Resource.Error -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        hasRequestedMore = false,
                        error = result.message ?: "An unexpected error occured"
                    )
                }
                is Resource.Loading -> {
                    state.value = state.value.copy(
                        isLoading = true,
                        hasRequestedMore = false
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun updatePost(post: ListItem.Post) {
        val postList = state.value.itemList.toMutableList()
        val position = postList.indexOfFirst { (it as? ListItem.Post)?.id == post.id }

        if (position > -1) {
            postList[position] = post
            state.value = state.value.copy(isLoading = false, itemList = postList)
        }
    }

    fun fetchNextPage() {
        if (state.value.error.isEmpty()) {
            state.value = state.value.copy(hasRequestedMore = true)
        }
    }

    fun refreshPostList() {
        viewModelScope.launch {
            state.value = State()

            delay(200)
            fetchPostList(groupId, state.value.offset)
        }
    }

    fun togglePostLike(post: ListItem.Post) {
        repository.toggleLike(apiKey, post.id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    updatePost(post.copy(likeCount = if (result.data == "insert") post.likeCount + 1 else post.likeCount - 1))
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

    init {
        groupId = savedStateHandle.get<Int>(ARG_PARAM1)?.also { groupId -> fetchPostList(groupId, state.value.offset) } ?: 0

        viewModelScope.launch {
            userFlow.collectLatest { user ->
                apiKey = user?.apiKey ?: ""
            }
        }
    }

    companion object {
        private const val ARG_PARAM1 = "group_id"
        private const val ARG_PARAM2 = "group_name"
    }

    data class State(
        var isLoading: Boolean = false,
        val itemList: List<ListItem> = mutableListOf(),
        var offset: Int = 0,
        var hasRequestedMore: Boolean = false,
        var error: String = ""
    )
}

class PostViewModelFactory(
    private val repository: PostRepository,
    private val preferenceManager: PreferenceManager,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            return PostViewModel(repository, preferenceManager, handle) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}