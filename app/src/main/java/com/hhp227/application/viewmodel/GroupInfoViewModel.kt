package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.app.AppController
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GroupInfoViewModel : ViewModel() {
    val state = MutableStateFlow(State())

    var requestType = 0

    var joinType = 0

    var groupId = 0

    var groupName = ""

    val repository = GroupRepository()

    fun sendRequest() {
        repository.requestToJoinOrCancel(AppController.getInstance().preferenceManager.user.apiKey, requestType, joinType, groupId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(isSuccess = result.data ?: false)
                }
                is Resource.Error -> {
                    state.value = state.value.copy(
                        isSuccess = false,
                        error = result.message.toString()
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