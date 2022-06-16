package com.hhp227.application.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.api.ApiService
import com.hhp227.application.dto.ListItem

class PostPagingSource(
    private val service: ApiService,
    private val groupId: Int
) : PagingSource<Int, ListItem.Post>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListItem.Post> {
        return try {
            val offset: Int = params.key ?: 0
            val data = service.getPostList(groupId, offset).posts
            LoadResult.Page(
                data = data,
                prevKey = null,
                nextKey = if (data.isEmpty()) null else offset + params.loadSize
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListItem.Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}