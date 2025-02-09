package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.GroupType
import kotlinx.coroutines.flow.*

class FindGroupViewModel internal constructor(
    private val repository: GroupRepository,
    preferenceManager: PreferenceManager
) : ViewModel() {
    val state = preferenceManager.getUserFlow()
        .flatMapLatest { repository.getGroupList(it?.apiKey ?: "", GroupType.NotJoined) }
        .catch { State(message = it.message) }
        .cachedIn(viewModelScope)
        .map { State(pagingData = it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            State(isLoading = true)
        )
        .asLiveData()

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "FindGroupViewModel onCleared")
    }

    data class State(
        val isLoading: Boolean = false,
        val pagingData: PagingData<GroupItem>? = PagingData.empty(),
        val message: String? = ""
    )
}

class FindGroupViewModelFactory(
    private val repository: GroupRepository,
    private val preferenceManager: PreferenceManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FindGroupViewModel::class.java)) {
            return FindGroupViewModel(repository, preferenceManager) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}