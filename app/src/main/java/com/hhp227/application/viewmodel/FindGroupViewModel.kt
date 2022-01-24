package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import com.hhp227.application.dto.GroupItem

class FindGroupViewModel : ViewModel() {
    val groupList: MutableList<GroupItem> by lazy { mutableListOf() }
}