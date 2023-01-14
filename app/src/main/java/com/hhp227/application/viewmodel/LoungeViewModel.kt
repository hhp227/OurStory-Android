package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hhp227.application.data.PostRepository
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.Resource
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LoungeViewModel internal constructor(
    private val repository: PostRepository,
    preferenceManager: PreferenceManager
) : ViewModel() {
    val user = MutableLiveData<User>()

    val posts: LiveData<PagingData<ListItem.Post>> = repository.getPostList(0).cachedIn(viewModelScope)

    val payload = MutableLiveData<ListItem.Post>()

    val state = MutableStateFlow(State())

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "LoungeViewModel onCleared")
    }

    fun togglePostLike(post: ListItem.Post) {
        repository.toggleLike(user.value?.apiKey ?: "", post.id)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        payload.postValue(post.copy(
                            likeCount = if (result.data == "insert") post.likeCount + 1 else post.likeCount - 1
                        ))
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
            }
            .launchIn(viewModelScope)
    }

    init {
        preferenceManager.userFlow
            .onEach { user ->
                this.user.postValue(user)
            }
            .launchIn(viewModelScope)
        Log.e("TEST", "LoungeViewModel init")
    }

    data class State(
        val isLoading: Boolean = false,
        val pagingData: PagingData<ListItem.Post> = PagingData.empty(),
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