package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.UserRepository
import com.hhp227.application.dto.Resource
import com.hhp227.application.dto.UserItem
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FriendViewModel(private val userRepository: UserRepository, preferenceManager: PreferenceManager) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableStateFlow(State())

    private fun fetchFriendList(offset: Int) {
        userRepository.getFriendList(apiKey, offset)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value.copy(
                            isLoading = false,
                            userItems = result.data ?: emptyList(),
                            offset = state.value.offset + (result.data?.size ?: 0),
                            hasRequestedMore = true
                        )
                    }
                    is Resource.Error -> {
                        state.value = state.value.copy(
                            isLoading = false,
                            hasRequestedMore = false,
                            error = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value.copy(
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

                    fetchFriendList(state.value.offset)
                }
        }
    }

    data class State(
        val isLoading: Boolean = false,
        val userItems: List<UserItem> = emptyList(),
        val offset: Int = 0,
        val hasRequestedMore: Boolean = false,
        val error: String = ""
    )
}

class FriendViewModelFactory(
    private val repository: UserRepository,
    private val preferenceManager: PreferenceManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendViewModel::class.java)) {
            return FriendViewModel(repository, preferenceManager) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}