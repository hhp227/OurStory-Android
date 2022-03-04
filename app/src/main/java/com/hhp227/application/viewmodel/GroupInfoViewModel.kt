package com.hhp227.application.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.app.AppController
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.dto.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GroupInfoViewModel(private val repository: GroupRepository, savedStateHandle: SavedStateHandle) : ViewModel() {
    val state = MutableStateFlow(State())

    val apiKey = AppController.getInstance().preferenceManager.user!!.apiKey

    val group: GroupItem.Group = savedStateHandle.get("group") ?: GroupItem.Group()

    val requestType = savedStateHandle.get("request_type") ?: 0

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "GroupInfoViewModel onCleared")
    }

    fun sendRequest() {
        repository.requestToJoinOrCancel(apiKey, requestType, group.joinType, group.id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(isSuccess = result.data ?: false)
                }
                is Resource.Error -> {
                    state.value = state.value.copy(
                        isSuccess = false,
                        error = result.message ?: "An unexpected error occured"
                    )
                }
                is Resource.Loading -> Unit
            }
        }.launchIn(viewModelScope)
    }

    data class State(
        val isSuccess: Boolean = false,
        val error: String = ""
    )
}

class GroupInfoViewModelFactory(
    private val repository: GroupRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(GroupInfoViewModel::class.java)) {
            return GroupInfoViewModel(repository, handle) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}