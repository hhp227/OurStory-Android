package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import com.hhp227.application.dto.PostItem

class Tab2ViewModel : ViewModel() {
    val postItems: MutableList<PostItem> by lazy { arrayListOf() }

    var groupId: Int = 0

    var groupName: String? = null

    data class State(
        var isLoading: Boolean = false,
        var postItems: List<PostItem> = emptyList(),
        var error: String = ""
    )
}