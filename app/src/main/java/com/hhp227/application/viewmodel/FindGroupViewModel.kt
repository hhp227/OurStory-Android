package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.app.AppController
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.dto.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FindGroupViewModel(private val repository: GroupRepository) : ViewModel() {
    val state = MutableStateFlow(State())

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
                        offset = state.value.offset + (result.data ?: emptyList()).size,
                        hasRequestedMore = true
                    )
                }
                is Resource.Error -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        groupList = result.data ?: emptyList(),
                        hasRequestedMore = false,
                        error = result.message ?: "An unexpected error occured"
                    )
                }
                is Resource.Loading -> {
                    state.value = state.value.copy(
                        isLoading = true,
                        hasRequestedMore = false
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun fetchNextPage() {
        if (state.value.hasRequestedMore) {
            fetchGroupList(offset = state.value.offset)
        }
    }

    init {
        fetchGroupList(state.value.offset)
    }

    data class State(
        val isLoading: Boolean = false,
        val offset: Int = 0,
        val groupList: List<GroupItem> = mutableListOf(),
        val hasRequestedMore: Boolean = false,
        val error: String = ""
    )
}

class FindGroupViewModelFactory(
    private val repository: GroupRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FindGroupViewModel::class.java)) {
            return FindGroupViewModel(repository) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}