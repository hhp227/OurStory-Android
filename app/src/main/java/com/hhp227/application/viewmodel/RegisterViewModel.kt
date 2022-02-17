package com.hhp227.application.viewmodel

import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.R
import com.hhp227.application.data.UserRepository
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class RegisterViewModel internal constructor(private val repository: UserRepository) : ViewModel() {
    val state = MutableStateFlow(State())

    private fun isNameValid(name: String): Boolean {
        return !TextUtils.isEmpty(name)
    }

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

    private fun isPasswordCheckValid(password: String, passwordCheck: String): Boolean {
        return password == passwordCheck
    }

    private fun isRegisterFormValid(name: String, email: String, password: String, passwordCheck: String): Boolean {
        return if (!isNameValid(name)) {
            state.value = state.value.copy(
                registerFormState = RegisterFormState(nameError = R.string.invalid_name)
            )
            false
        } else if (!isEmailValid(email)) {
            state.value = state.value.copy(
                registerFormState = RegisterFormState(emailError = R.string.invalid_email)
            )
            false
        } else if (!isPasswordValid(password)) {
            state.value = state.value.copy(
                registerFormState = RegisterFormState(passwordError = R.string.invalid_password)
            )
            false
        } else if (!isPasswordCheckValid(password, passwordCheck)) {
            state.value = state.value.copy(
                registerFormState = RegisterFormState(passwordCheckError = R.string.invalid_password_check)
            )
            false
        } else {
            true
        }
    }

    fun register(name: String, email: String, password: String, passwordCheck: String) {
        if (isRegisterFormValid(name, email, password, passwordCheck)) {
            repository.register(name, email, password).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = State(error = "")
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
            state.value = State(error = "register_input_correct")
    }

    data class State(
        val isLoading: Boolean = false,
        val registerFormState: RegisterFormState? = null,
        val error: String? = null
    )

    data class RegisterFormState(
        val nameError: Int? = null,
        val emailError: Int? = null,
        val passwordError: Int? = null,
        val passwordCheckError: Int? = null
    )
}

class RegisterViewModelFactory(
    private val repository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(repository) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}