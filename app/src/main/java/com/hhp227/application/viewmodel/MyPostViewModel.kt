package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel

class MyPostViewModel : ViewModel() {
    val postItems by lazy { arrayListOf(Any()) }
}