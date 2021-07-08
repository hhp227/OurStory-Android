package com.hhp227.application.dto

import android.os.Parcelable
import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable.Creator
import com.hhp227.application.dto.ImageItem

data class ImageItem(
    var id: Int = 0,
    var image: String? = null,
    var tag: String? = null,
    var bitmap: Bitmap? = null
) : Parcelable {
    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        image = parcel.readString()
        tag = parcel.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(image)
        dest.writeString(tag)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        val CREATOR: Creator<ImageItem> = object : Creator<ImageItem> {
            override fun createFromParcel(`in`: Parcel): ImageItem {
                return ImageItem(`in`)
            }

            override fun newArray(size: Int): Array<ImageItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}