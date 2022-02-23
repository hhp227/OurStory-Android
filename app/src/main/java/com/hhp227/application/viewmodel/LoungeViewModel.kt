package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.app.AppController
import com.hhp227.application.data.PostRepository
import com.hhp227.application.dto.ListItem
import com.hhp227.application.dto.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoungeViewModel internal constructor(private val repository: PostRepository) : ViewModel() {
    val state = MutableStateFlow(State())

    val apiKey = AppController.getInstance().preferenceManager.user.apiKey

    private fun fetchPostList(groupId: Int = 0, offset: Int) {
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

    fun updatePost(post: ListItem.Post) {
        val postList = state.value.itemList.toMutableList()
        val position = postList.indexOfFirst { (it as? ListItem.Post)?.id == post.id }

        if (position > -1) {
            postList[position] = post
            state.value = state.value.copy(isLoading = false, itemList = postList)
        }
    }

    fun fetchNextPage() {
        if (state.value.hasRequestedMore) {
            fetchPostList(offset = state.value.offset)
        }
    }

    fun refreshPostList() {
        //repository.refreshPostList(groupId, offset)
        viewModelScope.launch {
            state.value = State()

            delay(200)
            fetchPostList(offset = state.value.offset)
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
        fetchPostList(offset = state.value.offset)
    }

    data class State(
        var isLoading: Boolean = false,
        val itemList: List<ListItem> = mutableListOf(),
        var offset: Int = 0,
        var hasRequestedMore: Boolean = false,
        var error: String = ""
    )
}

class LoungeViewModelFactory(
    private val repository: PostRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoungeViewModel::class.java)) {
            return LoungeViewModel(repository) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}