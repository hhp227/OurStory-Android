package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.app.AppController
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GroupViewModel : ViewModel() {
    val state = MutableStateFlow(State())

    val repository = GroupRepository()

    val itemList: MutableList<Any> by lazy { arrayListOf() }

    var spanCount = 0

    fun getGroupList() {
        repository.getGroupList(AppController.getInstance().preferenceManager.user.apiKey).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        itemList = result.data ?: emptyList<Any>()
                    )
                }
                is Resource.Error -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        error = result.message.toString()
                    )
                }
                is Resource.Loading -> {
                    state.value = state.value.copy(
                        isLoading = true
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    init {
        getGroupList()
    }

    data class State(
        val isLoading: Boolean = false,
        val itemList: List<*> = emptyList<Any>(),
        val error: String = ""
    )
}