package com.hhp227.application.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.UserRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.Resource
import com.hhp227.application.model.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FriendViewModel(private val userRepository: UserRepository, preferenceManager: PreferenceManager) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableLiveData(State())

    private fun fetchFriendList(offset: Int) {
        userRepository.getFriendList(apiKey, offset)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value?.copy(
                            isLoading = false,
                            userItems = result.data ?: emptyList(),
                            offset = state.value!!.offset + (result.data?.size ?: 0),
                            hasRequestedMore = true
                        )
                    }
                    is Resource.Error -> {
                        state.value = state.value?.copy(
                            isLoading = false,
                            hasRequestedMore = false,
                            message = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(
                            isLoading = true,
                            hasRequestedMore = false
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    init {
        viewModelScope.launch {
            preferenceManager.userFlow
                .collectLatest { user ->
                    apiKey = user?.apiKey ?: ""

                    fetchFriendList(state.value!!.offset)
                }
        }
    }

    data class State(
        val isLoading: Boolean = false,
        val userItems: List<User> = emptyList(),
        val offset: Int = 0,
        val hasRequestedMore: Boolean = false,
        val message: String = ""
    )
}