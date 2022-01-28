package com.hhp227.application.dto

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageItem(
    var id: Int = 0,
    var image: String? = null,
    var tag: String? = null,
    var bitmap: Bitmap? = null
) : Parcelable, PostItem()