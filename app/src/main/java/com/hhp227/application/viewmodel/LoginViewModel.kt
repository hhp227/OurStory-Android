package com.hhp227.application.viewmodel

import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.*
import com.hhp227.application.R
import com.hhp227.application.data.UserRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.Resource
import com.hhp227.application.model.User
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LoginViewModel internal constructor(
    private val repository: UserRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    val state = MutableLiveData(State())

    val user: LiveData<User?> get() = preferenceManager.userFlow.asLiveData()

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
            state.postValue(state.value?.copy(emailError = R.string.invalid_email))
            false
        } else if (!isPasswordValid(password)) {
            state.postValue(state.value?.copy(
                emailError = null,
                passwordError = R.string.invalid_password
            ))
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
        if (isLoginFormValid(state.value!!.email, state.value!!.password)) {
            repository.login(state.value!!.email, state.value!!.password)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            state.value = state.value?.copy(
                                emailError = null,
                                passwordError = null,
                                isLoading = false,
                                user = result.data
                            )
                        }
                        is Resource.Error -> {
                            state.value = state.value?.copy(
                                emailError = null,
                                passwordError = null,
                                isLoading = false,
                                message = result.message ?: "An unexpected error occured"
                            )
                        }
                        is Resource.Loading -> {
                            state.value = state.value?.copy(
                                emailError = null,
                                passwordError = null,
                                isLoading = true,
                                message = ""
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
        } else
            state.value = state.value?.copy(
                emailError = null,
                passwordError = null,
                message = "login_input_correct"
            )
    }

    data class State(
        var email: String = "",
        var password: String = "",
        val emailError: Int? = null,
        val passwordError: Int? = null,
        val isLoading: Boolean = false,
        val user: User? = null,
        val message: String = ""
    )
}