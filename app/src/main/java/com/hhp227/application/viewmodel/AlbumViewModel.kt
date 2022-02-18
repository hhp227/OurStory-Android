package com.hhp227.application.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.PostRepository
import com.hhp227.application.dto.ListItem
import com.hhp227.application.dto.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AlbumViewModel internal constructor(private val repository: PostRepository, savedStateHandle: SavedStateHandle) : ViewModel() {
    val state = MutableStateFlow(State())

    val groupId: Int

    private fun fetchPostListWithImage(groupId: Int, offset: Int) {
        repository.getPostListWithImage(groupId, offset).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        postItems = state.value.postItems + (result.data ?: emptyList()),
                        offset = state.value.offset + (result.data?.size ?: 0)
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

    fun updatePost(post: ListItem.Post) {
        val postList = state.value.postItems.toMutableList()
        val position = postList.indexOfFirst { (it as? ListItem.Post)?.id == post.id }

        if (post.imageItemList.isEmpty()) {
            if (position > -1) {
                postList.removeAt(position)
            } else {
                return
            }
        } else {
            if (position > -1) {
                postList[position] = post
            } else {
                val idList = postList.map { (it as ListItem.Post).id }.plus(post.id).sortedDescending()
                val index = idList.indexOf(post.id)

                postList.add(index, post)
            }
        }
        if (postList.isNotEmpty()) {
            state.value = state.value.copy(postItems = postList)
        }
    }

    fun refreshPostList() {
        viewModelScope.launch {
            state.value = State()

            delay(200)
            fetchPostListWithImage(groupId, state.value.offset)
        }
    }

    init {
        groupId = savedStateHandle.get<Int>(ARG_PARAM1)?.also { groupId -> fetchPostListWithImage(groupId, state.value.offset) } ?: 0
    }

    companion object {
        private const val ARG_PARAM1 = "group_id"
    }

    data class State(
        var isLoading: Boolean = false,
        var postItems: List<ListItem> = emptyList(),
        var offset: Int = 0,
        var error: String = ""
    )
}

class AlbumViewModelFactory(
    private val repository: PostRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return AlbumViewModel(repository, handle) as T
    }
}
