package com.hhp227.application.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.model.Resource
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel internal constructor(
    private val repository: GroupRepository,
    preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private lateinit var apiKey: String

    private val groupId = savedStateHandle.get<Int>(ARG_PARAM1) ?: 0

    private val authorId = savedStateHandle.get<Int>(ARG_PARAM2)

    val state = MutableLiveData(State())

    val userFlow = preferenceManager.userFlow

    var isAuth: Boolean = false
        private set

    fun deleteGroup() {
        repository.removeGroup(apiKey, groupId, isAuth)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value?.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                    is Resource.Error -> {
                        state.value = state.value?.copy(
                            isLoading = false,
                            error = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(isLoading = true)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    init {
        viewModelScope.launch {
            userFlow.collectLatest { user ->
                apiKey = user?.apiKey ?: ""
                isAuth = user?.id == authorId
            }
        }
    }

    companion object {
        private const val ARG_PARAM1 = "group_id"
        private const val ARG_PARAM2 = "author_id"
    }

    data class State(
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val error: String = ""
    )
}

class SettingsViewModelFactory(
    private val repository: GroupRepository,
    private val preferenceManager: PreferenceManager,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository, preferenceManager, handle) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}