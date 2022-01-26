package com.hhp227.application.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.hhp227.application.dto.GalleryItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.flow

class ImageRepository(private val context: Context) {
    fun getImageList() = flow<Resource<Array<GalleryItem>>> {
        context.contentResolver?.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            null,
            null,
            null
        )?.use { imageCursor ->
            Array(imageCursor.count) {
                imageCursor.moveToPosition(it)
                GalleryItem(ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageCursor.getLong(imageCursor.getColumnIndex(MediaStore.Images.ImageColumns._ID))
                ), false)
            }
        }?.also { emit(Resource.Success(it)) }
    }

    companion object {
        @Volatile private var instance: ImageRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: ImageRepository(context).also { instance = it }
            }
    }

}