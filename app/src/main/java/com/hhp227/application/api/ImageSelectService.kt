package com.hhp227.application.api

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import android.util.Log
import com.hhp227.application.dto.GalleryItem

class ImageSelectService(private val contentResolver: ContentResolver) {
    fun getImageList(offset: Int, loadSize: Int): List<GalleryItem>? {
        return contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.ImageColumns._ID),
            null,
            null,
            "${MediaStore.Images.Media._ID} DESC"
        )?.use { imageCursor ->
            val listSize = imageCursor.count - offset

            if (listSize > 0) {
                List(if (listSize < loadSize) listSize else loadSize) {
                    imageCursor.moveToPosition(offset + it)
                    GalleryItem(
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            imageCursor.getLong(imageCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID))
                        ), false
                    )
                }
            } else
                emptyList()
        }
    }
}