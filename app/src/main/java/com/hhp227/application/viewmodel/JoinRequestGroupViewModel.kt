package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.dto.Resource
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class JoinRequestGroupViewModel internal constructor(private val repository: GroupRepository, preferenceManager: PreferenceManager) : ViewModel() {
    private val apiKey = preferenceManager.user?.apiKey ?: ""

    val state = MutableStateFlow(State())

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "JoinRequestGroupViewModel onCleared")
    }

    private fun fetchGroupList(offset: Int) {
        repository.getJoinRequestGroupList(apiKey).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        groupList = result.data ?: emptyList()
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
        var groupList: List<GroupItem> = emptyList(),
        val offset: Int = 0,
        val error: String = ""
    )
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
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}