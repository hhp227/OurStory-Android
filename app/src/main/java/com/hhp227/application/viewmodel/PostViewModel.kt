package com.hhp227.application.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.PostRepository
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.Resource
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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

    val posts: LiveData<PagingData<ListItem.Post>> = repository.getPostList(group.id).cachedIn(viewModelScope).asLiveData()

    val state = MutableLiveData(State())

    val user = preferenceManager.userFlow.asLiveData()

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "Tab1ViewModel onCleared")
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
                            error = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(isLoading = true)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    init {
        viewModelScope.launch {
            preferenceManager.userFlow.collectLatest { user ->
                apiKey = user?.apiKey ?: ""
            }
        }
    }

    companion object {
        private const val ARG_PARAM = "group"
    }

    data class State(
        var isLoading: Boolean = false,
        val payload: ListItem.Post = ListItem.Post(),
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