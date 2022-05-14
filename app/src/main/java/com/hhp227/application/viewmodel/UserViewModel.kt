package com.hhp227.application.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.UserRepository
import com.hhp227.application.dto.Resource
import com.hhp227.application.dto.UserItem
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository, preferenceManager: PreferenceManager, savedStateHandle: SavedStateHandle) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableStateFlow(State())

    val user = savedStateHandle.get<UserItem>("user")

    val userFlow = preferenceManager.userFlow

    private fun isFriend() {
        user?.also { user ->
            repository.isFriend(apiKey, user.id)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            state.value = state.value.copy(
                                isLoading = false,
                                isFriend = (result.data ?: 0) > 0,
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
                }
                .launchIn(viewModelScope)
        }
    }

    fun addFriend() {
        user?.also { user ->
            repository.toggleFriend(apiKey, user.id)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            state.value = state.value.copy(
                                isLoading = false,
                                result = result.data ?: ""
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
                }
                .launchIn(viewModelScope)
        }
    }

    init {
        viewModelScope.launch {
            userFlow.collectLatest { user ->
                apiKey = user?.apiKey ?: ""

                isFriend()
            }
        }
    }

    data class State(
        val isLoading: Boolean = false,
        val result: String = "",
        val isFriend: Boolean = false,
        val error: String = ""
    )
}

class UserViewModelFactory(
    private val repository: UserRepository,
    private val preferenceManager: PreferenceManager,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(repository, preferenceManager, handle) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}