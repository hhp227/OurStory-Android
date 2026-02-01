package com.hhp227.application.viewmodel

import android.os.Bundle
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.AlbumRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.ListItem
import kotlinx.coroutines.delay
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

    private fun fetchPostListWithImage(id: Int = group.id) {
        repository.getPostListWithImage(id)
            .cachedIn(viewModelScope)
            .catch { state.value = state.value?.copy(message = it.message) }
            .onEach(::setPagingData)
            .launchIn(viewModelScope)
    }

    private fun setPagingData(pagingData: PagingData<ListItem.Post>?) {
        state.value = state.value?.copy(pagingData = pagingData)
    }

    fun onDeletePost(post: ListItem.Post) {
        val pagingData = state.value?.pagingData?.filter { it.id != post.id }

        setPagingData(pagingData)
    }

    fun refresh() {
        repository.clearCache(group.id)
    }

    init {
        fetchPostListWithImage(group.id)
    }

    companion object {
        private const val ARG_PARAM = "group"
    }

    data class State(
        var isLoading: Boolean = false,
        val pagingData: PagingData<ListItem.Post>? = PagingData.empty(),
        val message: String? = ""
    )
}