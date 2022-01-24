package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.dto.GroupItem

class FindGroupViewModel : ViewModel() {
    val groupList: MutableList<GroupItem> by lazy { mutableListOf() }

    val repository = GroupRepository()

    fun getGroupList() {

    }

    init {
        getGroupList()
    }
}