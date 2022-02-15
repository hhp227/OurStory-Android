package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import com.hhp227.application.dto.ListItem

class MyPostViewModel : ViewModel() {
    val postItems: MutableList<ListItem> by lazy { arrayListOf(ListItem.Loader) }
}