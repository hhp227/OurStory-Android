package com.hhp227.application.viewmodel

import android.util.Log
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class FindGroupViewModel internal constructor(
    private val repository: GroupRepository,
    preferenceManager: PreferenceManager
) : ViewModel() {
    val state = MutableLiveData(State())

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "FindGroupViewModel onCleared")
    }

    private fun setPagingData(pagingData: PagingData<GroupItem>?) {
        state.value = state.value?.copy(pagingData = pagingData)
    }

    init {
        viewModelScope.launch {
            preferenceManager.userFlow
                .flatMapLatest { repository.getGroupList(it?.apiKey ?: "", GroupType.NotJoined) }
                .catch { state.value = state.value?.copy(message = it.message) }
                .cachedIn(viewModelScope)
                .collectLatest(::setPagingData)
        }
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