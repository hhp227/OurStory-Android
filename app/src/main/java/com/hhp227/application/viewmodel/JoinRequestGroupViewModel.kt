package com.hhp227.application.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
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

class JoinRequestGroupViewModelFactory(
    private val repository: GroupRepository,
    private val preferenceManager: PreferenceManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JoinRequestGroupViewModel::class.java)) {
            return JoinRequestGroupViewModel(repository, preferenceManager) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}
