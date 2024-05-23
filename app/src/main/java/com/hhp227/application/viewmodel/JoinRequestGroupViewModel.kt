package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertHeaderItem
import com.hhp227.application.R
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.GroupType
import com.hhp227.application.viewmodel.GroupViewModel.State
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class JoinRequestGroupViewModel internal constructor(
    private val repository: GroupRepository,
    preferenceManager: PreferenceManager
) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableLiveData(State())

    private fun setPagingData(pagingData: PagingData<GroupItem>?) {
        state.value = state.value?.copy(pagingData = pagingData)
    }

    init {
        preferenceManager.userFlow
            .flatMapLatest {
                apiKey = it?.apiKey ?: ""
                return@flatMapLatest repository.getGroupList(apiKey, GroupType.RequestedToJoin)
            }
            .cachedIn(viewModelScope)
            .onEach(::setPagingData)
            .launchIn(viewModelScope)
    }

    data class State(
        val isLoading: Boolean = false,
        val pagingData: PagingData<GroupItem>? = PagingData.empty()
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
