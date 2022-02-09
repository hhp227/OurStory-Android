package com.hhp227.application.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.PostRepository
import com.hhp227.application.dto.PostItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val state = MutableStateFlow(State())

    val repository = PostRepository()

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

    fun updatePost(intent: Intent) {
        val position = intent.getIntExtra("position", 0)
        val post = intent.getParcelableExtra("post") ?: PostItem.Post()
        val postList = state.value.itemList.toMutableList()
        postList[position] = post
        state.value = state.value.copy(itemList = postList)
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

    init {
        fetchPostList(offset = state.value.offset)
    }

    data class State(
        var isLoading: Boolean = false,
        val itemList: List<PostItem> = mutableListOf(),
        var offset: Int = 0,
        var hasRequestedMore: Boolean = false,
        var error: String = ""
    )
}