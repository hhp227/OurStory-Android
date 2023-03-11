package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class JoinRequestGroupViewModel internal constructor(private val repository: GroupRepository, preferenceManager: PreferenceManager) : ViewModel() {
    private lateinit var apiKey: String

    val groups get() = repository.getJoinRequestGroupList(apiKey).cachedIn(viewModelScope)

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "JoinRequestGroupViewModel onCleared")
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