package com.hhp227.application.viewmodel

import android.os.Bundle
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.UserRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.Resource
import com.hhp227.application.model.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: UserRepository,
    private val preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableLiveData(State())

    val otherUser = savedStateHandle.get<User>("user")

    private fun isFriend() {
        otherUser?.also { user ->
            repository.isFriend(apiKey, user.id)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            state.value = state.value?.copy(
                                isLoading = false,
                                isFriend = (result.data ?: 0) > 0,
                            )
                        }
                        is Resource.Error -> {
                            state.value = state.value?.copy(
                                isLoading = false,
                                message = result.message ?: "An unexpected error occured"
                            )
                        }
                        is Resource.Loading -> {
                            state.value = state.value?.copy(
                                isLoading = true
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun addFriend() {
        otherUser?.also { user ->
            repository.toggleFriend(apiKey, user.id)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            state.value = state.value?.copy(
                                isLoading = false,
                                result = result.data ?: ""
                            )
                        }
                        is Resource.Error -> {
                            state.value = state.value?.copy(
                                isLoading = false,
                                message = result.message ?: "An unexpected error occured"
                            )
                        }
                        is Resource.Loading -> {
                            state.value = state.value?.copy(
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
            preferenceManager.userFlow.collectLatest { user ->
                apiKey = user?.apiKey ?: ""

                isFriend()
            }
        }
    }

    data class State(
        val isLoading: Boolean = false,
        val result: String = "",
        val isFriend: Boolean = false,
        val message: String = ""
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