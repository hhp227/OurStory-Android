package com.hhp227.application.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.UserRepository
import com.hhp227.application.dto.UserItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginViewModel internal constructor(private val repository: UserRepository) : ViewModel() {
    val state = MutableStateFlow(State())

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "LoginViewModel onCleared")
    }

    fun login(email: String, password: String) {

        // 폼에 데이터가 비어있는지 확인
        if (email.isNotEmpty() && password.isNotEmpty()) {
            repository.login(email, password).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = State(user = result.data)
                    }
                    is Resource.Error -> {
                        state.value = State(error = result.message ?: "An unexpected error occured")
                    }
                    is Resource.Loading -> {
                        state.value = State(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        } else
            state.value = State(error = "login_input_correct")
    }

    data class State(
        val isLoading: Boolean = false,
        val user: UserItem? = null,
        val error: String = ""
    )
}

class LoginViewModelFactory(
    private val repository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}