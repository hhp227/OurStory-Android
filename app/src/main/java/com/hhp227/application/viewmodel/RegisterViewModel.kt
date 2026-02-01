package com.hhp227.application.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.R
import com.hhp227.application.data.UserRepository
import com.hhp227.application.model.Resource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.regex.Pattern

class RegisterViewModel internal constructor(
    private val repository: UserRepository
) : ViewModel() {
    val state = MutableLiveData(State())

    private fun isNameValid(name: String): Boolean {
        return !TextUtils.isEmpty(name)
    }

    private fun isEmailValid(email: String): Boolean {
        return Pattern.matches("^(.+)@(.+)\$", email)
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun isPasswordAndConfirmationValid(password: String, confirmedPassword: String): Boolean {
        return password == confirmedPassword
    }

    private fun isRegisterFormValid(name: String, email: String, password: String, confirmedPassword: String): Boolean {
        return if (!isNameValid(name)) {
            state.value = state.value?.copy(nameError = R.string.invalid_name)
            false
        } else if (!isEmailValid(email)) {
            state.value = state.value?.copy(emailError = R.string.invalid_email)
            false
        } else if (!isPasswordValid(password)) {
            state.value = state.value?.copy(passwordError = R.string.invalid_password)
            false
        } else if (!isPasswordAndConfirmationValid(password, confirmedPassword)) {
            state.value = state.value?.copy(passwordCheckError = R.string.invalid_password_check)
            false
        } else {
            true
        }
    }

    fun register() {
        if (isRegisterFormValid(state.value!!.name, state.value!!.email, state.value!!.password, state.value!!.confirmPassword)) {
            repository.register(state.value!!.name, state.value!!.email, state.value!!.password)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            state.value = State(isLoading = false, message = "")
                        }
                        is Resource.Error -> {
                            state.value = State(isLoading = false, message = result.message ?: "An unexpected error occured")
                        }
                        is Resource.Loading -> {
                            state.value = State(isLoading = true)
                        }
                    }
                }
                .launchIn(viewModelScope)
        } else
            state.value = state.value?.copy(message = "register_input_correct")
    }

    data class State(
        var name: String = "",
        var email: String = "",
        var password: String = "",
        var confirmPassword: String = "",
        val nameError: Int? = null,
        val emailError: Int? = null,
        val passwordError: Int? = null,
        val passwordCheckError: Int? = null,
        val isLoading: Boolean = false,
        val message: String? = null
    )
}