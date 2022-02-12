package com.hhp227.application.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.PostRepository
import com.hhp227.application.dto.PostItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostViewModel internal constructor(private val repository: PostRepository, savedStateHandle: SavedStateHandle): ViewModel() {
    val state = MutableStateFlow(State())

    val groupId: Int

    var groupName: String? = null

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "Tab1ViewModel onCleared")
    }

    private fun fetchPostList(groupId: Int, offset: Int) {
        repository.getPostList(groupId, offset).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        itemList = state.value.itemList + (result.data ?: emptyList()),
                        offset = state.value.offset + (result.data?.size ?: 0),
                        hasRequestedMore = true
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

    fun updatePost(post: PostItem.Post) {
        val postList = state.value.itemList.toMutableList()
        val position = postList.indexOfFirst { (it as? PostItem.Post)?.id == post.id }
        postList[position] = post
        state.value = state.value.copy(itemList = postList)
    }

    fun fetchNextPage() {
        if (state.value.hasRequestedMore) {
            fetchPostList(groupId, state.value.offset)
        }
    }

    fun refreshPostList() {
        viewModelScope.launch {
            state.value = State()

            delay(200)
            fetchPostList(groupId, state.value.offset)
        }
    }

    init {
        groupId = savedStateHandle.get<Int>(ARG_PARAM1)?.also { groupId -> fetchPostList(groupId, state.value.offset) } ?: 0
    }

    companion object {
        private const val ARG_PARAM1 = "group_id"
        private const val ARG_PARAM2 = "group_name"
    }

    data class State(
        var isLoading: Boolean = false,
        val itemList: List<PostItem> = mutableListOf(),
        var offset: Int = 0,
        var hasRequestedMore: Boolean = false,
        var error: String = ""
    )
}

class PostViewModelFactory(
    private val repository: PostRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            return PostViewModel(repository, handle) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}