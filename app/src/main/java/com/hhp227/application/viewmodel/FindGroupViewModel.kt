package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FindGroupViewModel : ViewModel() {
    val groupList: MutableList<GroupItem> by lazy { mutableListOf() }

    val state = MutableStateFlow(State())

    val repository = GroupRepository()

    fun getGroupList() {

    }

    init {
        getGroupList()
    }

    data class State(
        val isLoading: Boolean = false,
        val groupList: List<GroupItem> = emptyList(),
        val error: String = ""
    )
}