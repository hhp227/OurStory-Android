package com.hhp227.application.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hhp227.application.data.UserRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.User
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MemberViewModel internal constructor(
    private val repository: UserRepository,
    preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableLiveData(State())

    val group: GroupItem.Group = savedStateHandle[ARG_PARAM] ?: GroupItem.Group()

    private fun fetchUserList(groupId: Int) {
        repository.getUserList(groupId)
            .catch { state.value = state.value?.copy(message = it.message) }
            .cachedIn(viewModelScope)
            .onEach(::setPagingData)
            .launchIn(viewModelScope)
    }

    private fun setPagingData(pagingData: PagingData<User>?) {
        state.value = state.value?.copy(pagingData = pagingData)
    }

    fun setLoading(isLoading: Boolean) {
        state.value = state.value?.copy(isLoading = isLoading)
    }

    fun refresh() {
        repository.clearCache(0)
    }

    init {
        fetchUserList(group.id)
        preferenceManager.userFlow
            .onEach { user ->
                apiKey = user?.apiKey ?: ""
                state.value = state.value?.copy(user = user)
            }
            .launchIn(viewModelScope)
    }

    companion object {
        private const val ARG_PARAM = "group"
    }

    data class State(
        val isLoading: Boolean = false,
        val pagingData: PagingData<User>? = PagingData.empty(),
        val user: User? = null,
        val message: String? = ""
    )
}