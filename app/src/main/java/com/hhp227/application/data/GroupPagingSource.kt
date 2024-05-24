package com.hhp227.application.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.api.GroupService
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.GroupType
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

class GroupPagingSource(
    private val groupService: GroupService,
    private val groupDao: GroupDao,
    private val apiKey: String,
    private val type: GroupType
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
            val key = offset + groupDao.count
            val nextKey = key + loadSize
            val prevKey = key - loadSize
            val response = when (type) {
                GroupType.Joined -> groupService.getMyGroupList(apiKey, offset, loadSize)
                GroupType.NotJoined -> groupService.getNotJoinedGroupList(apiKey, offset, loadSize)
                GroupType.RequestedToJoin -> groupService.getMyGroupList(apiKey, offset, loadSize, 1)
            }

            if (!response.error) {
                val data = response.data ?: emptyList()

                groupDao.insertAll(type.ordinal, data)
                Log.e("TEST", "data: ${response.data?.map { it.id to it.groupName }}")
                delay(2000)
                LoadResult.Page(
                    data = groupDao.getGroupList(type.ordinal, key, nextKey),
                    prevKey = if (offset == 0) null else prevKey,
                    nextKey = if (data.isEmpty()) null else nextKey
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