package com.hhp227.application.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import com.hhp227.application.activity.CreateGroupActivity
import com.hhp227.application.app.AppController
import com.hhp227.application.helper.BitmapUtil
import java.io.File
import java.io.IOException

class CreateGroupViewModel : ViewModel() {
    lateinit var uri: Uri

    lateinit var currentPhotoPath: String

    val apiKey: String? by lazy { AppController.getInstance().preferenceManager.user.apiKey }

    var bitMap: Bitmap? = null

    var joinType = false

    fun setBitmap(bitmapUtil: BitmapUtil) {
        bitMap = try {
            bitmapUtil.bitmapResize(uri, 200)?.let {
                val ei = currentPhotoPath.let(::ExifInterface)
                val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                return@let bitmapUtil.rotateImage(it, when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90F
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180F
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270F
                    else -> 0F
                })
            }
        } catch (e: IOException) {
            null
        }
    }
}