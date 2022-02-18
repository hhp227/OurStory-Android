package com.hhp227.application.data

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import com.hhp227.application.dto.GalleryItem
import com.hhp227.application.dto.Resource
import kotlinx.coroutines.flow.flow

class ImageRepository {
    fun getImageList(contentResolver: ContentResolver) = flow<Resource<List<GalleryItem>>> {
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            null,
            null,
            null
        )?.use { imageCursor ->
            List(imageCursor.count) {
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

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: ImageRepository().also { instance = it }
            }
    }

}