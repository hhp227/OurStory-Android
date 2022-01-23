package com.hhp227.application.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.UserRepository
import com.hhp227.application.dto.User
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginViewModel : ViewModel() {
    val state = MediatorLiveData<State>()

    val repository = UserRepository()

    fun login(email: String, password: String) {

        // 폼에 데이터가 비어있는지 확인
        if (email.isNotEmpty() && password.isNotEmpty()) {
            repository.login(email, password).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.postValue(State(user = result.data))
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
            state.postValue(State(error = "login_input_correct"))
    }

    data class State(
        val isLoading: Boolean = false,
        val user: User? = null,
        val error: String = ""
    )
}