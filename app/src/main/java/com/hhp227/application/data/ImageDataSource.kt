package com.hhp227.application.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.api.ImageSelectService
import com.hhp227.application.dto.GalleryItem

class ImageDataSource(private val imageSelectService: ImageSelectService) : PagingSource<Int, GalleryItem>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryItem> {
        val offset: Int = params.key ?: 0
        val result = imageSelectService.getImageList(offset, params.loadSize) ?: emptyList()
        return try {
            LoadResult.Page(
                data = result,
                prevKey = if (offset == 0) null else offset,
                nextKey = if (result.isEmpty()) null else offset + params.loadSize
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, GalleryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}