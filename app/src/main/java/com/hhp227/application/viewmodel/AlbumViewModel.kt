package com.hhp227.application.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.AlbumRepository
import com.hhp227.application.data.PostRepository
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.Resource
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AlbumViewModel internal constructor(
    private val repository: AlbumRepository,
    preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val group = savedStateHandle.get<GroupItem.Group>(ARG_PARAM) ?: GroupItem.Group()

    val state = MutableLiveData(State())

    val userFlow = preferenceManager.userFlow

    private fun fetchPostListWithImage(id: Int = group.id) {
        repository.getPostListWithImage(id)
            .cachedIn(viewModelScope)
            .catch { state.value = state.value?.copy(message = it.message) }
            .onEach(::setPagingData)
            .launchIn(viewModelScope)
        /*repository.getPostListWithImage(id, 0)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value?.copy(
                            isLoading = false,
                            postItems = state.value?.postItems?.plus((result.data ?: emptyList())) ?: emptyList(),
                            offset = state.value?.offset?.plus((result.data?.size ?: 0)) ?: 0
                        )
                    }
                    is Resource.Error -> {
                        state.value = state.value?.copy(
                            isLoading = false,
                            message = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(
                            isLoading = true
                        )
                    }
                }
            }
            .launchIn(viewModelScope)*/
    }

    private fun setPagingData(pagingData: PagingData<ListItem.Post>) {
        state.value = state.value?.copy(pagingData = pagingData)
    }

    fun updatePost(post: ListItem.Post) {
        val postList = state.value?.postItems?.toMutableList()
        val position = postList?.indexOfFirst { (it as? ListItem.Post)?.id == post.id } ?: -1

        if (post.attachment.imageItemList.isEmpty()) {
            if (position > -1) {
                postList?.removeAt(position)
            } else {
                return
            }
        } else {
            if (position > -1) {
                postList?.set(position, post)
            } else {
                val idList = postList?.map { (it as ListItem.Post).id }?.plus(post.id)?.sortedDescending()
                val index = idList?.indexOf(post.id) ?: -1

                postList?.add(index, post)
            }
        }
        if (postList?.isNotEmpty() == true) {
            state.value = state.value?.copy(postItems = postList)
        }
    }

    fun refreshPostList() {
        viewModelScope.launch {
            state.value = State()

            delay(200)
            fetchPostListWithImage(group.id)
        }
    }

    init {
        fetchPostListWithImage(group.id)
    }

    companion object {
        private const val ARG_PARAM = "group"
    }

    data class State(
        var isLoading: Boolean = false,
        var postItems: List<ListItem> = emptyList(),
        val pagingData: PagingData<ListItem.Post> = PagingData.empty(),
        var offset: Int = 0,
        val message: String? = ""
    )
}

class AlbumViewModelFactory(
    private val repository: AlbumRepository,
    private val preferenceManager: PreferenceManager,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return AlbumViewModel(repository, preferenceManager, handle) as T
    }
}
