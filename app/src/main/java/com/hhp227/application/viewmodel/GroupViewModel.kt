package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.app.AppController
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GroupViewModel : ViewModel() {
    val state = MutableStateFlow(State())

    val repository = GroupRepository()

    var spanCount = 0

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "GroupViewModel onCleared")
    }

    private fun fetchGroupList(offset: Int) {
        repository.getMyGroupList(AppController.getInstance().preferenceManager.user.apiKey, offset).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        itemList = state.value.itemList + (result.data ?: emptyList()),
                        offset = state.value.offset + (result.data?.size ?: 0),
                        hasRequestedMore = true
                    )
                }
                is Resource.Error -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        itemList = emptyList(),
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
            fetchGroupList(state.value.offset)
        }
    }

    fun refreshGroupList() {
        viewModelScope.launch {
            state.value = State()

            delay(200)
            fetchGroupList(state.value.offset)
        }
    }

    init {
        fetchGroupList(state.value.offset)
    }

    data class State(
        val isLoading: Boolean = false,
        val itemList: List<GroupItem> = listOf(GroupItem.Title), // getString(R.string.joined_group)
        val offset: Int = 0,
        var hasRequestedMore: Boolean = false,
        val error: String = ""
    )
}