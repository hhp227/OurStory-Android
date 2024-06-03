package com.hhp227.application.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.api.UserService
import com.hhp227.application.model.User
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.max

class UserPagingSource(
    private val userService: UserService,
    private val userDao: UserDao,
    private val groupId: Int
) : PagingSource<Int, User>() {
    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            if (userDao.isCacheEmpty(groupId)) null
            else state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        return try {
            val offset: Int = params.key ?: 0
            val loadSize: Int = params.loadSize
            val key = max(0, offset + userDao.getCount(groupId))
            val nextKey = key + loadSize
            val prevKey = key - loadSize
            val response = userService.getUserList(groupId, key, loadSize)

            if (offset == 0) userDao.deleteAll(groupId)
            if (!response.error) {
                val data = response.data ?: emptyList()

                userDao.insertAll(groupId, data)
                delay(2000)
                LoadResult.Page(
                    data = userDao.getUserList(groupId, key, nextKey),
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
}