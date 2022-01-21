package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel

class FindGroupViewModel : ViewModel() {
    val groupList: MutableList<Any> by lazy { mutableListOf() }
}