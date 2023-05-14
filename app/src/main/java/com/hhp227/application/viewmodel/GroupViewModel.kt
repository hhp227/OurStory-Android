package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.*
import com.hhp227.application.R
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GroupViewModel internal constructor(
    private val repository: GroupRepository,
    preferenceManager: PreferenceManager
) : ViewModel() {
    private lateinit var apiKey: String

    val groups: LiveData<PagingData<GroupItem>> get() = repository.getMyGroupList(apiKey)
        .map { it.insertHeaderItem(item = GroupItem.Title(R.string.joined_group)) }
        .cachedIn(viewModelScope)

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "GroupViewModel onCleared")
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

class GroupViewModelFactory(
    private val repository: GroupRepository,
    private val preferenceManager: PreferenceManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupViewModel::class.java)) {
            return GroupViewModel(repository, preferenceManager) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}