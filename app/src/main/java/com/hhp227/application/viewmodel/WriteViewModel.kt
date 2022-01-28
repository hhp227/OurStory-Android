package com.hhp227.application.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.hhp227.application.app.AppController
import com.hhp227.application.dto.ImageItem
import com.hhp227.application.dto.PostItem

class WriteViewModel : ViewModel() {
    val apiKey: String? by lazy { AppController.getInstance().preferenceManager.user.apiKey }

    val itemList: MutableList<PostItem> by lazy { arrayListOf() }

    lateinit var post: PostItem.Post

    lateinit var currentPhotoPath: String

    lateinit var photoURI: Uri

    var type = 0

    var groupId = 0

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "WriteViewModel onCleared")
    }

    data class State(
        val itemList: MutableList<PostItem> = mutableListOf()
    )
}