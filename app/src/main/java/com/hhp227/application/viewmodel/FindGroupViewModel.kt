package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.Resource
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FindGroupViewModel internal constructor(private val repository: GroupRepository, preferenceManager: PreferenceManager) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableStateFlow(State())

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "FindGroupViewModel onCleared")
    }

    fun fetchGroupList(offset: Int) {
        repository.getNotJoinedGroupList(apiKey, offset)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value.copy(
                            isLoading = false,
                            groupList = state.value.groupList.plus(result.data ?: emptyList()),
                            offset = state.value.offset + (result.data ?: emptyList()).size,
                            hasRequestedMore = false
                        )
                    }
                    is Resource.Error -> {
                        state.value = state.value.copy(
                            isLoading = false,
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
            }
            .launchIn(viewModelScope)
    }

    fun refreshGroupList() {
        viewModelScope.launch {
            state.value = State()

            delay(200)
            fetchGroupList(state.value.offset)
        }
    }

    fun fetchNextPage() {
        if (state.value.error.isEmpty()) {
            state.value = state.value.copy(hasRequestedMore = true)
        }
    }

    init {
        viewModelScope.launch {
            preferenceManager.userFlow
                .collectLatest { user ->
                    apiKey = user?.apiKey ?: ""

                    fetchGroupList(state.value.offset)
                }
        }
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
    private val repository: GroupRepository,
    private val preferenceManager: PreferenceManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FindGroupViewModel::class.java)) {
            return FindGroupViewModel(repository, preferenceManager) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}