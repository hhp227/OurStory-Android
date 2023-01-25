package com.hhp227.application.viewmodel

import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.*
import com.hhp227.application.R
import com.hhp227.application.data.UserRepository
import com.hhp227.application.model.User
import com.hhp227.application.model.Resource
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel internal constructor(
    private val repository: UserRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    val state = MutableStateFlow(State())

    val userFlow = preferenceManager.userFlow

    private fun isEmailValid(email: String): Boolean {
        return if (!email.contains('@')) {
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
            state.update {
                it.copy(emailError = R.string.invalid_email)
            }
            false
        } else if (!isPasswordValid(password)) {
            state.update {
                it.copy(passwordError = R.string.invalid_password)
            }
            false
        } else {
            true
        }
    }

    fun updateEmail(email: String) {
        state.update {
            it.copy(email = email)
        }
    }

    fun updatePassword(password: String) {
        state.update {
            it.copy(password = password)
        }
    }

    fun storeUser(user: User) {
        viewModelScope.launch {
            preferenceManager.storeUser(user)
        }
    }

    fun login() {
        if (isLoginFormValid(state.value.email, state.value.password)) {
            repository.login(state.value.email, state.value.password)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            state.value = state.value.copy(isLoading = false, user = result.data)
                        }
                        is Resource.Error -> {
                            state.value = state.value.copy(isLoading = false, error = result.message ?: "An unexpected error occured")
                        }
                        is Resource.Loading -> {
                            state.value = state.value.copy(isLoading = true)
                        }
                    }
                }
                .launchIn(viewModelScope)
        } else
            state.value = state.value.copy(error = "login_input_correct")
    }

    data class State(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val user: User? = null,
        val error: String = "",
        val emailError: Int? = null,
        val passwordError: Int? = null
    )
}

class LoginViewModelFactory(
    private val repository: UserRepository,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository, preferenceManager) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}