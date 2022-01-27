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

class GroupViewModel : ViewModel() {
    val state = MutableStateFlow(State())

    val repository = GroupRepository()

    var spanCount = 0

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "GroupViewModel onCleared")
    }

    fun fetchGroupList() {
        repository.getMyGroupList(AppController.getInstance().preferenceManager.user.apiKey).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        itemList = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    state.value = state.value.copy(
                        isLoading = false,
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
        fetchGroupList()
    }

    data class State(
        val isLoading: Boolean = false,
        val itemList: List<GroupItem> = emptyList(),
        val error: String = ""
    )
}