package com.hhp227.application.viewmodel

import android.text.TextUtils
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
import kotlinx.coroutines.launch

class LoginViewModel internal constructor(
    private val repository: UserRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    val email = MutableStateFlow("")

    val password = MutableStateFlow("")

    val state = MutableStateFlow(State())

    val loginFormState = MutableStateFlow(LoginFormState())

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
            loginFormState.value = LoginFormState(emailError = R.string.invalid_email)
            false
        } else if (!isPasswordValid(password)) {
            loginFormState.value = LoginFormState(passwordError = R.string.invalid_password)
            false
        } else {
            true
        }
    }

    fun storeUser(user: User) {
        viewModelScope.launch {
            preferenceManager.storeUser(user)
        }
    }

    fun login() {
        if (isLoginFormValid(email.value, password.value)) {
            repository.login(email.value, password.value)
                .onEach { result ->
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
                }
                .launchIn(viewModelScope)
        } else
            state.value = State(error = "login_input_correct")
    }

    data class State(
        val isLoading: Boolean = false,
        val user: User? = null,
        val error: String = ""
    )

    data class LoginFormState(
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