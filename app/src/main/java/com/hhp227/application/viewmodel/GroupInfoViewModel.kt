package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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