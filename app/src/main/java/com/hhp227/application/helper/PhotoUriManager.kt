package com.hhp227.application.helper

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.*

class PhotoUriManager(private val appContext: Context) {
    private val photoCollection by lazy {
        if (Build.VERSION.SDK_INT > 28) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
    }

    private val resolver by lazy { appContext.contentResolver }

    private fun buildPhotoDetails() = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, generateFilename())
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }

    @SuppressLint("SimpleDateFormat")
    private fun generateFilename() = "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}_.jpg"

    fun buildNewUri() = resolver.insert(photoCollection, buildPhotoDetails())
}