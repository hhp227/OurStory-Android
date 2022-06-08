package com.hhp227.application.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.api.ApiService
import com.hhp227.application.dto.ListItem

class PostPagingSource(
    private val service: ApiService
) : PagingSource<Int, ListItem.Post>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListItem.Post> {
        return try {
            LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListItem.Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.itemsBefore
        }
    }
}