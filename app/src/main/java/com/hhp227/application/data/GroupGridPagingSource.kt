package com.hhp227.application.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.api.GroupService
import com.hhp227.application.model.GroupItem
import retrofit2.HttpException
import java.io.IOException

class GroupGridPagingSource(
    private val groupService: GroupService,
    private val apiKey: String
): PagingSource<Int, GroupItem>() {
    override fun getRefreshKey(state: PagingState<Int, GroupItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            (state.closestItemToPosition(anchorPosition) as? GroupItem.Group)?.id
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GroupItem> {
        return try {
            val nextPage: Int = params.key ?: 0
            val loadSize: Int = params.loadSize
            val response = groupService.getMyGroupList(apiKey, nextPage, loadSize)

            if (!response.error) {
                val data = response.data?.toMutableList() ?: mutableListOf()

                LoadResult.Page(
                    data = data,
                    prevKey = if (nextPage == 0) null else nextPage - 1,
                    nextKey = if (params.key == null) nextPage + 3 else nextPage + 1
                )
            } else {
                LoadResult.Error(Throwable(response.message))
            }
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }
}