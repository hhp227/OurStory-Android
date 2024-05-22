package com.hhp227.application.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.api.GroupService
import com.hhp227.application.model.GroupItem
import retrofit2.HttpException
import java.io.IOException

class GroupPagingSource(
private val groupService: GroupService,
private val groupDao: GroupDao,
private val apiKey: String,
private val type: Int
): PagingSource<Int, GroupItem>() {
    override fun getRefreshKey(state: PagingState<Int, GroupItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GroupItem> {
        return try {
            val offset = params.key ?: 0
            val loadSize = params.loadSize
            val response = when (type) {
                0 -> groupService.getMyGroupList(apiKey, offset, loadSize)
                1 -> groupService.getNotJoinedGroupList(apiKey, offset, loadSize)
                else -> groupService.getMyGroupList(apiKey, offset, loadSize, 0)
            }

            LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }
}