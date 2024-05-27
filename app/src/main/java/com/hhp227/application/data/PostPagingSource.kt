package com.hhp227.application.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.api.PostService
import com.hhp227.application.model.ListItem
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

class PostPagingSource(
    private val postService: PostService,
    private val postDao: PostDao,
    private val groupId: Int
) : PagingSource<Int, ListItem.Post>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListItem.Post> {
        return try {
            val offset: Int = params.key ?: 0
            val loadSize: Int = params.loadSize
            val key = offset + postDao.getCount(groupId)
            val nextKey = key + loadSize
            val prevKey = key - loadSize
            val response = postService.getPostList(groupId, key, loadSize)

            if (!response.error) {
                val data = response.data ?: emptyList()

                Log.e("TEST", "offset: $offset data: ${data.map { it.id }}")
                postDao.insertAll(groupId, data)
                Log.e("TEST", "startIndex: $key, cached: ${postDao.cachedList(groupId)?.map { it.id }}")
                delay(2000)
                LoadResult.Page(
                    data = postDao.getPostList(groupId, key, nextKey),
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
            if (postDao.isCacheEmpty(groupId)) null
            else state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }
}