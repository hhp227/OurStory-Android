package com.hhp227.application.dto

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class GalleryItem(val uri: Uri, var isSelected: Boolean) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Uri::class.java.classLoader)!!,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, flags)
        parcel.writeByte(if (isSelected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GalleryItem> {
        override fun createFromParcel(parcel: Parcel): GalleryItem {
            return GalleryItem(parcel)
        }

        override fun newArray(size: Int): Array<GalleryItem?> {
            return arrayOfNulls(size)
        }
    }
}