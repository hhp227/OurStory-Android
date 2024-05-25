package com.hhp227.application.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.PostRepository
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.Resource
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostViewModel internal constructor(
    private val repository: PostRepository,
    preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private lateinit var apiKey: String

    val group: GroupItem.Group = savedStateHandle.get<GroupItem.Group>(ARG_PARAM) ?: GroupItem.Group()

    val state = MutableLiveData(State())

    val user = preferenceManager.userFlow.asLiveData()

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "Tab1ViewModel onCleared")
    }

    private fun setPagingData(pagingData: PagingData<ListItem.Post>?) {
        state.value = state.value?.copy(pagingData = pagingData)
    }

    fun togglePostLike(post: ListItem.Post) {
        repository.toggleLike(apiKey, post.id)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value?.copy(payload = post.copy(
                            likeCount = if (result.data == "insert") post.likeCount + 1 else post.likeCount - 1
                        ))
                    }
                    is Resource.Error -> {
                        state.value = state.value?.copy(
                            isLoading = false,
                            message = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(isLoading = true)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onDeletePost(post: ListItem.Post) {
        val pagingData = state.value?.pagingData?.filter { it.id != post.id }

        setPagingData(pagingData)
    }

    fun refresh() {
        repository.clearCache(group.id)
    }

    init {
        viewModelScope.launch {
            preferenceManager.userFlow.collectLatest { user ->
                apiKey = user?.apiKey ?: ""
            }
        }
        repository.getPostList(group.id)
            .cachedIn(viewModelScope)
            .catch { state.value = state.value?.copy(message = it.message) }
            .onEach(::setPagingData)
            .launchIn(viewModelScope)
    }

    companion object {
        private const val ARG_PARAM = "group"
    }

    data class State(
        val isLoading: Boolean = false,
        val payload: ListItem.Post = ListItem.Post(),
        val pagingData: PagingData<ListItem.Post>? = PagingData.empty(),
        val message: String? = ""
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