package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val itemList: MutableList<Any> by lazy { arrayListOf() }
}