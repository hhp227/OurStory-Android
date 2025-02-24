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
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.User
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

    private var isAuth: Boolean = false

    val group = savedStateHandle.get<GroupItem.Group>(ARG_PARAM) ?: GroupItem.Group()

    val state = MutableLiveData(State())

    fun deleteGroup() {
        repository.removeGroup(apiKey, group.id, isAuth)
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
                            message = result.message ?: "An unexpected error occured"
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
            preferenceManager.userFlow.collectLatest { user ->
                state.value = state.value?.copy(user = user)
                apiKey = user?.apiKey ?: ""
                isAuth = user?.id == group.authorId
            }
        }
    }

    companion object {
        private const val ARG_PARAM = "group"
    }

    data class State(
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val user: User? = null,
        val message: String = ""
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