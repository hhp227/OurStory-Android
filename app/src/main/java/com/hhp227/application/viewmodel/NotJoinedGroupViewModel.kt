package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import com.hhp227.application.dto.GroupItem

class NotJoinedGroupViewModel : ViewModel() {
    val groupList: MutableList<GroupItem> = mutableListOf()
}