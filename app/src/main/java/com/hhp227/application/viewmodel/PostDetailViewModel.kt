package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import com.hhp227.application.app.AppController
import com.hhp227.application.dto.PostItem
import com.hhp227.application.dto.UserItem

class PostDetailViewModel : ViewModel() {
    val itemList: MutableList<Any> by lazy { arrayListOf() }

    val user: UserItem by lazy { AppController.getInstance().preferenceManager.user }

    lateinit var post: PostItem.Post

    var groupName: String? = null

    var isBottom = false

    var isUpdate = false
}