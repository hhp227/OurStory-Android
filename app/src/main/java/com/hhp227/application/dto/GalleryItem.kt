package com.hhp227.application.dto

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GalleryItem(val uri: Uri, var isSelected: Boolean) : Parcelable