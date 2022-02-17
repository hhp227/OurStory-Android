package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.app.AppController
import com.hhp227.application.data.PostRepository
import com.hhp227.application.dto.ListItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MyPostViewModel internal constructor(private val repository: PostRepository) : ViewModel() {
    val postItems: MutableList<ListItem> by lazy { arrayListOf(ListItem.Loader) }

    val apiKey = AppController.getInstance().preferenceManager.user.apiKey

    val state = MutableStateFlow(State())

    fun fetchPostList(offset: Int) {
        repository.getUserPostList(apiKey, offset).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        postItems = state.value.postItems + (result.data ?: emptyList()),
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

    init {
        fetchPostList(state.value.offset)
    }

    data class State(
        val isLoading: Boolean = false,
        val postItems: List<ListItem> = emptyList(),
        val offset: Int = 0,
        val hasRequestedMore: Boolean = false,
        val error: String = ""
    )
}

class MyPostViewModelFactory(
    private val repository: PostRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPostViewModel::class.java)) {
            return MyPostViewModel(repository) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}