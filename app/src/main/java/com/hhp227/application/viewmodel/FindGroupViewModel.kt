package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FindGroupViewModel internal constructor(
    private val repository: GroupRepository,
    preferenceManager: PreferenceManager
) : ViewModel() {
    private lateinit var apiKey: String

    val groups: LiveData<PagingData<GroupItem>> get() = repository.getNotJoinedGroupList(apiKey).cachedIn(viewModelScope)

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "FindGroupViewModel onCleared")
    }

    init {
        viewModelScope.launch {
            preferenceManager.userFlow
                .collectLatest { user ->
                    apiKey = user?.apiKey ?: ""
                }
        }
    }
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