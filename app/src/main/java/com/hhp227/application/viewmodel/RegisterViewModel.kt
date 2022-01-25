package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.UserRepository
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class RegisterViewModel : ViewModel() {
    val state = MutableStateFlow(State())

    val repository = UserRepository()

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "RegisterViewModel onCleared")
    }

    fun register(name: String, email: String, password: String) {
        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            repository.register(name, email, password).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = State(error = "")
                    }
                    is Resource.Error -> {
                        state.value = State(error = result.message.toString())
                    }
                    is Resource.Loading -> {
                        state.value = State(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        } else
            state.value = State(error = "입력값이 없습니다.")
    }

    data class State(
        val isLoading: Boolean = false,
        val error: String? = null
    )
}