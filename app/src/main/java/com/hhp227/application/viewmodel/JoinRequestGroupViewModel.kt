package com.hhp227.application.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.GroupType
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class JoinRequestGroupViewModel internal constructor(
    private val repository: GroupRepository,
    preferenceManager: PreferenceManager
) : ViewModel() {
    val state = MutableLiveData(State())

    private fun setPagingData(pagingData: PagingData<GroupItem>?) {
        state.value = state.value?.copy(pagingData = pagingData)
    }

    fun onDeleteGroup(groupId: Int) {
        val pagingData = state.value?.pagingData?.filter { (it as GroupItem.Group).id != groupId }

        setPagingData(pagingData)
    }

    fun refresh() {
        repository.clearCache(GroupType.RequestedToJoin)
    }

    init {
        preferenceManager.userFlow
            .flatMapLatest { repository.getGroupList(it?.apiKey ?: "", GroupType.RequestedToJoin) }
            .catch { state.value = state.value?.copy(message = it.message) }
            .cachedIn(viewModelScope)
            .onEach(::setPagingData)
            .launchIn(viewModelScope)
    }

    data class State(
        val isLoading: Boolean = false,
        val pagingData: PagingData<GroupItem>? = PagingData.empty(),
        val message: String? = ""
    )
}