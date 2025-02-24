package com.hhp227.application.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GroupInfoViewModel internal constructor(
    private val repository: GroupRepository,
    preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableLiveData(State())

    val group: GroupItem.Group = savedStateHandle["group"] ?: GroupItem.Group()

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "GroupInfoViewModel onCleared")
    }

    fun sendRequest(isSignUp: Boolean) {
        repository.requestToJoinOrCancel(apiKey, isSignUp, group.joinType, group.id)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value?.copy(groupId = result.data ?: -1)
                    }
                    is Resource.Error -> {
                        state.value = state.value?.copy(
                            groupId = -1,
                            message = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    init {
        viewModelScope.launch {
            preferenceManager.userFlow
                .collectLatest { user ->
                    apiKey = user?.apiKey ?: ""
                }
        }
    }

    data class State(
        val groupId: Int = -1,
        val message: String = ""
    )
}

class GroupInfoViewModelFactory(
    private val repository: GroupRepository,
    private val preferenceManager: PreferenceManager,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(GroupInfoViewModel::class.java)) {
            return GroupInfoViewModel(repository, preferenceManager, handle) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}