package com.hhp227.application.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.data.UserRepository
import com.hhp227.application.model.User
import com.hhp227.application.model.Resource
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MemberViewModel internal constructor(
    private val repository: UserRepository,
    preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val state = MutableLiveData(State())

    val userFlow = preferenceManager.userFlow

    val group: GroupItem.Group

    private fun fetchUserList(groupId: Int) {
        repository.getUserList(groupId)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value?.copy(
                            isLoading = false,
                            users = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        state.value = state.value?.copy(
                            isLoading = false,
                            message = result.message ?: ""
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(isLoading = true)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    init {
        group = savedStateHandle.get<GroupItem.Group>(ARG_PARAM)?.also { group -> fetchUserList(group.id) } ?: GroupItem.Group()
    }

    companion object {
        private const val ARG_PARAM = "group"
    }

    data class State(
        val isLoading: Boolean = false,
        val users: List<User> = emptyList(),
        val message: String = ""
    )
}

class MemberViewModelFactory(
    private val repository: UserRepository,
    private val preferenceManager: PreferenceManager,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(MemberViewModel::class.java)) {
            return MemberViewModel(repository, preferenceManager, handle) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}