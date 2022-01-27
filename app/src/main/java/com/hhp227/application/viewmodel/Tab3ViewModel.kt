package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import com.hhp227.application.dto.MemberItem

class Tab3ViewModel : ViewModel() {
    val memberItems by lazy { mutableListOf<MemberItem>() }

    var groupId = 0
}