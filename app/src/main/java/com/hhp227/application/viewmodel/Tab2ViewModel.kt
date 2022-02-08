package com.hhp227.application.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.PostRepository
import com.hhp227.application.dto.PostItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class Tab2ViewModel internal constructor(
    private val repository: PostRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val state = MutableStateFlow(State())

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
        savedStateHandle.get<Int>(ARG_PARAM1)?.also { groupId -> fetchPostWithImage(groupId, 0) }
        //savedStateHandle.get<String>(ARG_PARAM2)?.also { Log.e("TEST", "Tab2ViewModel init Test: $it") }
    }

    companion object {
        private const val ARG_PARAM1 = "group_id"
        private const val ARG_PARAM2 = "group_name"
    }

    data class State(
        var isLoading: Boolean = false,
        var postItems: List<PostItem> = emptyList(),
        var error: String = ""
    )
}

class Tab2ViewModelFactory(
    private val repository: PostRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return Tab2ViewModel(repository, handle) as T
    }
}
