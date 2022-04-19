package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.UserRepository
import com.hhp227.application.dto.UserItem
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FriendViewModel(private val userRepository: UserRepository, preferenceManager: PreferenceManager) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableStateFlow(State())

    private fun fetchFriendList(offset: Int) {
        userRepository.getFriendList(apiKey, offset)
    }

    init {
        viewModelScope.launch {
            preferenceManager.userFlow.collectLatest { user ->
                apiKey = user?.apiKey ?: ""

                fetchFriendList(state.value.offset)
            }
        }
        Log.e("TEST", "FriendViewModel init")
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