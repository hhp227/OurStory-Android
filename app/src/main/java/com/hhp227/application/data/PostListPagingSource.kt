package com.hhp227.application.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.api.PostService
import com.hhp227.application.model.ListItem
import kotlinx.coroutines.delay

class PostListPagingSource(
    private val postService: PostService,
    private val groupId: Int
) : PagingSource<Int, ListItem.Post>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListItem.Post> {
        return try {
            val nextPage: Int = params.key ?: 0
            val loadSize: Int = params.loadSize
            val data = postService.getPostList(groupId, nextPage, loadSize).posts

            delay(2000)
            LoadResult.Page(
                data = data,
                prevKey = if (nextPage == 0) null else nextPage - 1,
                nextKey = nextPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListItem.Post>): Int {
        return 0
    }
}