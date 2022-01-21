package com.hhp227.application.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.hhp227.application.app.AppController
import com.hhp227.application.dto.ImageItem

class WriteViewModel : ViewModel() {
    val apiKey: String? by lazy { AppController.getInstance().preferenceManager.user.apiKey }

    val itemList: MutableList<Any> by lazy { arrayListOf() }

    lateinit var content: String

    lateinit var currentPhotoPath: String

    lateinit var photoURI: Uri

    var imageList: ArrayList<ImageItem>? = null

    var postId = 0

    var type = 0

    var groupId = 0
}