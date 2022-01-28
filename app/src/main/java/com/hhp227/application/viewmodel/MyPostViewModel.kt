package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import com.hhp227.application.dto.PostItem

class MyPostViewModel : ViewModel() {
    val postItems: MutableList<PostItem> by lazy { arrayListOf(PostItem.Loader) }
}