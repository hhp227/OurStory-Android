package com.hhp227.application.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.api.PostService
import com.hhp227.application.model.ListItem
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

class PostListPagingSource(
    private val postService: PostService,
    private val postDao: PostDao,
    private val groupId: Int
) : PagingSource<Int, ListItem.Post>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListItem.Post> {
        return try {
            val nextPage: Int = params.key ?: 0
            val loadSize: Int = params.loadSize
            val data = postService.getPostList(groupId, nextPage, loadSize).data ?: emptyList()
            val startIndex = nextPage * loadSize + postDao.index
            val endIndex = startIndex + loadSize

            Log.e("TEST", "nextPage: $nextPage data: ${data.map { it.id }} cached: ${postDao.cachedPostList.map { it.id }}")
            postDao.insertAll(data)
            Log.e("TEST", "startIndex: $startIndex, cached: ${postDao.cachedPostList.map { it.id }}")
            delay(2000)
            LoadResult.Page(
                data = postDao.getPostList(startIndex, endIndex),
                prevKey = if (nextPage == 0) null else nextPage - 1,
                nextKey = if (params.key == null) nextPage + 3 else nextPage + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListItem.Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }
}