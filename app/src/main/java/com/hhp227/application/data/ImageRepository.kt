package com.hhp227.application.data

import android.content.ContentResolver
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hhp227.application.api.ImageSelectService
import com.hhp227.application.model.GalleryItem
import kotlinx.coroutines.flow.Flow

class ImageRepository {
    fun getImageDataStream(contentResolver: ContentResolver): Flow<PagingData<GalleryItem>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 15),
            pagingSourceFactory = { ImageDataSource(ImageSelectService.getInstance(contentResolver)) }
        ).flow
    }

    companion object {
        @Volatile private var instance: ImageRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: ImageRepository().also { instance = it }
            }
    }
}