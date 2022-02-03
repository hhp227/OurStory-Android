package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.PostRepository
import com.hhp227.application.dto.PostItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.*

class MainViewModel : ViewModel() {
    val itemList: MutableList<PostItem> by lazy { arrayListOf() }

    val state = MutableStateFlow(State())

    val repository = PostRepository()

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "MainViewModel onCleared ${state.value.offset}, ${state.value.hasRequestedMore}")
    }

    fun fetchPostList(groupId: Int, offset: Int) {
        repository.getPostList(groupId, offset).onCompletion { cause ->
            when (cause) {
                //state.value.hasRequestedMore = false
            }
        }.onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        itemList = result.data ?: emptyList(),
                        offset = result.data?.size ?: 0
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
                        isLoading = true
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    init {
        //fetchPostList(0, 0)
    }

    data class State(
        var isLoading: Boolean = false,
        val itemList: List<PostItem> = mutableListOf(),
        var offset: Int = 0,
        var hasRequestedMore: Boolean = false,
        var error: String = ""
    )
}