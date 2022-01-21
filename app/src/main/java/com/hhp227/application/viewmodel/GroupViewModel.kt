package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import com.hhp227.application.app.AppController

class GroupViewModel : ViewModel() {
    val itemList: MutableList<Any> by lazy { arrayListOf() }

    val apiKey: String? by lazy { AppController.getInstance().preferenceManager.user.apiKey }

    var spanCount = 0
}