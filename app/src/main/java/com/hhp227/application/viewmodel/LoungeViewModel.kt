package com.hhp227.application.viewmodel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hhp227.application.data.PostRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.Resource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoungeViewModel internal constructor(
    private val repository: PostRepository,
    preferenceManager: PreferenceManager
) : ViewModel() {
    private lateinit var apiKey: String

    val posts: LiveData<PagingData<ListItem.Post>> = repository.getPostList(0).cachedIn(viewModelScope)

    val state = MutableLiveData(State())

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
        preferenceManager.userFlow
            .onEach { user ->
                apiKey = user?.apiKey ?: ""
            }
            .launchIn(viewModelScope)
    }

    data class State(
        val payload: ListItem.Post = ListItem.Post(),
        val isLoading: Boolean = false,
        val pagingData: PagingData<ListItem.Post> = PagingData.empty(), // 지금은 사용하지 않음
        val error: String = ""
    )
}

class LoungeViewModelFactory(
    private val repository: PostRepository,
    private val preferenceManager: PreferenceManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoungeViewModel::class.java)) {
            return LoungeViewModel(repository, preferenceManager) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}