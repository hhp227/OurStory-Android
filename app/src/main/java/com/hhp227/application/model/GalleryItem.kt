package com.hhp227.application.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GalleryItem(val uri: Uri, var isSelected: Boolean) : Parcelable