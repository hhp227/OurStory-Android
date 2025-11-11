package com.hhp227.application.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
        var payload: ListItem.Post = ListItem.Post(),
        var isLoading: Boolean = false,
        var pagingData: PagingData<ListItem.Post>? = PagingData.empty(),
        var user: User? = null,
        var message: String? = ""
    )
}

/*
아래 코드는 iOS에서 pagingData가 필터 처리가 되지 않아 통일성을 위해 임의로 주석 처리함
 */
/*class LoungeViewModel internal constructor(
    private val repository: PostRepository,
    preferenceManager: PreferenceManager
) : ViewModel() {
    private val deletedPosts = MutableStateFlow(mutableSetOf<ListItem.Post>())

    private val posts = repository.getPostList(0)
        .cachedIn(viewModelScope)
        .map { Resource.Success(it) }
        .catch { Resource.Error<PagingData<ListItem.Post>>(it.message.toString()) }

    private val payload = MutableStateFlow(ListItem.Post())

    val state: LiveData<State> = combine(
        posts,
        payload,
        deletedPosts,
        preferenceManager.userFlow
    ) { result, payload, deletedPosts, user ->
        when (result) {
            is Resource.Loading<*> -> {
                State(isLoading = true)
            }
            is Resource.Success -> {
                State(
                    payload = payload,
                    isLoading = false,
                    pagingData = result.data?.filter { !deletedPosts.contains(it) },
                    user = user
                )
            }
            is Resource.Error<*> -> {
                State(
                    payload = payload,
                    isLoading = false,
                    message = result.message,
                    user = user
                )
            }
            else -> State()
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = State(isLoading = true)
        )
        .asLiveData()

    fun togglePostLike(post: ListItem.Post) {
        viewModelScope.launch {
            repository.toggleLike(state.value?.user?.apiKey ?: "", post.id)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            payload.value = post.copy(
                                likeCount = if (result.data == "insert") post.likeCount + 1 else post.likeCount - 1
                            )
                        }
                    }
                }
        }
    }

    fun onDeletePost(post: ListItem.Post) {
        deletedPosts.value.add(post)
    }

    fun refresh() {
        repository.clearCache(0)
    }

    data class State(
        var payload: ListItem.Post = ListItem.Post(),
        var isLoading: Boolean = false,
        var pagingData: PagingData<ListItem.Post>? = PagingData.empty(),
        var user: User? = null,
        var message: String? = ""
    )
}*/

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