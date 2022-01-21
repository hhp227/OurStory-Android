package com.hhp227.application.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.hhp227.application.app.AppController

class CreateGroupViewModel : ViewModel() {
    val apiKey: String? by lazy { AppController.getInstance().preferenceManager.user.apiKey }

    var bitMap: Bitmap? = null

    var joinType = false
}