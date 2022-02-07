package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.PostRepository
import com.hhp227.application.dto.PostItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class Tab2ViewModel : ViewModel() {
    val postItems: MutableList<PostItem> by lazy { arrayListOf() }

    val repository = PostRepository()

    val state = MutableStateFlow(State())

    var groupId: Int = 0

    var groupName: String? = null

    fun fetchPostWithImage(groupId: Int, offset: Int) {
        repository.getPostWithImage(groupId, offset).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        postItems = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    state.value = state.value.copy(
                        isLoading = false,
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
        //fetchPostWithImage(groupId, 0)
    }

    data class State(
        var isLoading: Boolean = false,
        var postItems: List<PostItem> = emptyList(),
        var error: String = ""
    )
}