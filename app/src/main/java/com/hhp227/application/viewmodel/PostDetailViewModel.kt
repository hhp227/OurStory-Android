package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import com.hhp227.application.app.AppController
import com.hhp227.application.dto.User

class PostDetailViewModel : ViewModel() {
    val itemList: MutableList<Any> by lazy { arrayListOf() }

    val user: User by lazy { AppController.getInstance().preferenceManager.user }

    var myUserId = 0

    var userId = 0

    var postId = 0

    var position = 0

    var groupId = 0

    var groupName: String? = null

    var isBottom = false

    var isUpdate = false
}