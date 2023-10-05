package com.hhp227.application.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.api.GroupService
import com.hhp227.application.model.GroupItem

class GroupListPagingSource(
    private val groupService: GroupService,
    private val apiKey: String,
    private val type: Int
) : PagingSource<Int, GroupItem>() {
    override fun getRefreshKey(state: PagingState<Int, GroupItem>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GroupItem> {
        return try {
            val nextPage: Int = params.key ?: 0
            val loadSize: Int = params.loadSize
            val data =
                if (type == 0) groupService.getNotJoinedGroupList(apiKey, nextPage, loadSize).data
                else groupService.getMyGroupList(apiKey, nextPage, loadSize, 1).data
            LoadResult.Page(
                data = data ?: emptyList(),
                prevKey = if (nextPage == 0) null else nextPage - 1,
                nextKey = if (params.key == null) nextPage + 3 else nextPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}