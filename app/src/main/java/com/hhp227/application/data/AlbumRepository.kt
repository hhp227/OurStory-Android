package com.hhp227.application.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hhp227.application.api.PostService
import com.hhp227.application.model.ListItem
import kotlinx.coroutines.flow.Flow

class AlbumRepository(
    private val postService: PostService,
    private val localDataSource: AlbumDao
) {
    fun getPostListWithImage(groupId: Int): Flow<PagingData<ListItem.Post>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 10),
            pagingSourceFactory = { AlbumPagingSource(postService, localDataSource, groupId) }
        ).flow
    }

    fun clearCache(groupId: Int) {
        localDataSource.deleteAll(groupId)
    }

    companion object {
        @Volatile private var instance: AlbumRepository? = null

        fun getInstance(postService: PostService, albumDao: AlbumDao) =
            instance ?: synchronized(this) {
                instance ?: AlbumRepository(postService, albumDao).also { instance = it }
            }
    }
}