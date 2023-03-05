package com.hhp227.application.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.api.GroupService
import com.hhp227.application.model.GroupItem

class GroupListPagingSource(
    private val groupService: GroupService,
    private val apiKey: String
) : PagingSource<Int, GroupItem>() {
    override fun getRefreshKey(state: PagingState<Int, GroupItem>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GroupItem> {
        return try {
            val nextPage: Int = params.key ?: 0
            val loadSize: Int = params.loadSize
            val data = groupService.getNotJoinedGroupList(apiKey, nextPage, loadSize).groups
            LoadResult.Page(
                data = data,
                prevKey = if (nextPage == 0) null else nextPage - 1,
                nextKey = nextPage + 1
                )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}