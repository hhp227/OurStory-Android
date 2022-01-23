package com.hhp227.application.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.UserRepository
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class RegisterViewModel : ViewModel() {
    val state = MediatorLiveData<State>()

    val repository = UserRepository()

    fun register(name: String, email: String, password: String) {
        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            repository.register(name, email, password).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.postValue(State())
                    }
                    is Resource.Error -> {
                        state.postValue(State(error = result.message.toString()))
                    }
                    is Resource.Loading -> {
                        state.postValue(State(isLoading = true))
                    }
                }
            }.launchIn(viewModelScope)
        } else
            state.postValue(State(error = "입력값이 없습니다."))
    }

    data class State(
        val isLoading: Boolean = false,
        val error: String = ""
    )
}