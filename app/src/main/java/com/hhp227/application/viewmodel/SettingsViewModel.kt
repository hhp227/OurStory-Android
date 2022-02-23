package com.hhp227.application.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.app.AppController
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.dto.UserItem
import com.hhp227.application.dto.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SettingsViewModel(private val repository: GroupRepository, savedStateHandle: SavedStateHandle) : ViewModel() {
    val state = MutableStateFlow(State())

    val user: UserItem = AppController.getInstance().preferenceManager.user

    val groupId = savedStateHandle.get<Int>(ARG_PARAM1) ?: 0

    val authorId = savedStateHandle.get<Int>(ARG_PARAM2)

    val isAuth = user.id == authorId

    fun deleteGroup() {
        repository.removeGroup(user.apiKey, groupId, isAuth).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                is Resource.Error -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        error = result.message ?: "An unexpected error occured"
                    )
                }
                is Resource.Loading -> {
                    state.value = state.value.copy(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
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
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository, handle) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}