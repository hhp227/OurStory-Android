package com.hhp227.application.viewmodel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.hhp227.application.data.PostRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.Resource
import com.hhp227.application.model.User
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoungeViewModel internal constructor(
    private val repository: PostRepository,
    preferenceManager: PreferenceManager
) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableLiveData(State())

    private fun setPagingData(pagingData: PagingData<ListItem.Post>?) {
        state.value = state.value?.copy(pagingData = pagingData)
    }

    fun togglePostLike(post: ListItem.Post) {
        repository.toggleLike(apiKey, post.id)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value?.copy(
                            payload = post.copy(
                                likeCount = if (result.data == "insert") post.likeCount + 1 else post.likeCount - 1
                            ),
                            isLoading = false
                        )
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
        repository.clearCache(0)
    }

    init {
        preferenceManager.userFlow
            .onEach { user ->
                apiKey = user?.apiKey ?: ""
                state.value = state.value?.copy(user = user)
            }
            .launchIn(viewModelScope)
        repository.getPostList(0)
            .catch { state.value = state.value?.copy(message = it.message) }
            .cachedIn(viewModelScope)
            .onEach(::setPagingData)
            .launchIn(viewModelScope)
    }

    data class State(
        val payload: ListItem.Post = ListItem.Post(),
        val isLoading: Boolean = false,
        val pagingData: PagingData<ListItem.Post>? = PagingData.empty(),
        val user: User? = null,
        val message: String? = ""
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