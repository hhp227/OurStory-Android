package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import com.hhp227.application.dto.PostItem

class Tab1ViewModel : ViewModel() {
    val postItems: MutableList<PostItem> by lazy { arrayListOf(PostItem.Loader) }

    var groupId: Int = 0

    var groupName: String? = null
}