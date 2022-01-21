package com.hhp227.application.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.hhp227.application.app.AppController
import com.hhp227.application.dto.User

class MyInfoViewModel : ViewModel() {
    val user: User by lazy { AppController.getInstance().preferenceManager.user }

    lateinit var currentPhotoPath: String

    lateinit var photoURI: Uri
}