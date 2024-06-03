package com.hhp227.application.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.api.PostService
import com.hhp227.application.model.ListItem
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.max

class AlbumPagingSource(
    private val postService: PostService,
    private val albumDao: AlbumDao,
    private val groupId: Int
) : PagingSource<Int, ListItem.Post>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListItem.Post> {
        return try {
            val offset: Int = params.key ?: 0
            val loadSize: Int = params.loadSize
            val key = max(0, offset + albumDao.getCount(groupId))
            val nextKey = key + loadSize
            val prevKey = key - loadSize
            val response = postService.getPostListWithImage(groupId, key, loadSize)

            if (offset == 0) albumDao.deleteAll(groupId)
            if (!response.error) {
                val data = response.data ?: emptyList()

                albumDao.insertAll(groupId, data)
                delay(2000)
                LoadResult.Page(
                    data = albumDao.getPostList(groupId, key, nextKey),
                    prevKey = if (offset == 0) null else prevKey,
                    nextKey = if (data.isEmpty()) null else nextKey
                )
            } else {
                LoadResult.Error(
                    Throwable(response.message)
                )
            }
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListItem.Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            if (albumDao.isCacheEmpty(groupId)) null
            else state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }
}