package com.hhp227.application.viewmodel

import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.*
import com.hhp227.application.R
import com.hhp227.application.data.UserRepository
import com.hhp227.application.dto.UserItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginViewModel internal constructor(private val repository: UserRepository) : ViewModel() {
    val state = MutableStateFlow(State())

    private fun isEmailValid(email: String): Boolean {
        return if (email.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } else {
            !TextUtils.isEmpty(email)
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun isLoginFormValid(email: String, password: String): Boolean {
        return if (!isEmailValid(email)) {
            state.value = state.value.copy(
                loginFormState = LoginFormState(emailError = R.string.invalid_email)
            )
            false
        } else if (!isPasswordValid(password)) {
            state.value = state.value.copy(
                loginFormState = LoginFormState(passwordError = R.string.invalid_password)
            )
            false
        } else {
            true
        }
    }

    fun login(email: String, password: String) {
        if (isLoginFormValid(email, password)) {
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
        val loginFormState: LoginFormState? = null,
        val error: String = ""
    )

    data class LoginFormState(
        val emailError: Int? = null,
        val passwordError: Int? = null
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