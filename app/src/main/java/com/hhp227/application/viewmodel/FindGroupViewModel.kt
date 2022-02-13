package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.app.AppController
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FindGroupViewModel : ViewModel() {
    val state = MutableStateFlow(State())

    val repository = GroupRepository()

    val apiKey = AppController.getInstance().preferenceManager.user.apiKey

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "FindGroupViewModel onCleared")
    }

    private fun fetchGroupList(offset: Int) {
        repository.getNotJoinedGroupList(apiKey, offset).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        groupList = state.value.groupList.plus(result.data ?: emptyList()),
                        offset = state.value.offset + (result.data ?: emptyList()).size
                    )
                }
                is Resource.Error -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        groupList = result.data ?: emptyList(),
                        error = result.message ?: "An unexpected error occured"
                    )
                }
                is Resource.Loading -> {
                    state.value = state.value.copy(
                        isLoading = true
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    init {
        fetchGroupList(state.value.offset)
    }

    data class State(
        val isLoading: Boolean = false,
        val offset: Int = 0,
        val groupList: List<GroupItem> = mutableListOf(),
        val error: String = ""
    )
}