package com.hhp227.application.viewmodel

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

    fun getGroupList(offset: Int) {
        repository.getNotJoinedGroupList(AppController.getInstance().preferenceManager.user.apiKey, offset).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        groupList = state.value.groupList.plus(result.data ?: emptyList()).toMutableList(),
                        offset = state.value.offset + (result.data ?: emptyList()).size
                    )
                }
                is Resource.Error -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        groupList = (result.data ?: emptyList()).toMutableList(),
                        error = result.message.toString()
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
        getGroupList(0)
    }

    data class State(
        val isLoading: Boolean = false,
        var offset: Int = 0,
        val groupList: MutableList<GroupItem> = mutableListOf(),
        val error: String = ""
    )
}