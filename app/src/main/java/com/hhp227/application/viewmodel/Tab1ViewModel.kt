package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel

class Tab1ViewModel : ViewModel() {
    val postItems by lazy { arrayListOf(Any()) }

    var groupId: Int = 0

    var groupName: String? = null
}