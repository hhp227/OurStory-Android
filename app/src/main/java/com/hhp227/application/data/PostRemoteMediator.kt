package com.hhp227.application.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.hhp227.application.api.PostService
import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.model.ListItem
import retrofit2.http.Query

/*import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.hhp227.application.api.PostService
import com.hhp227.application.model.ListEntity

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val postDb: PostDatabase,
    private val postApi: PostService
) : RemoteMediator<Int, ListEntity.Post>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, ListEntity.Post>): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(
                    endOfPaginationReached = true
                )
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()

                    if (lastItem == null) {
                        1
                    } else {
                        (lastItem.id / state.config.pageSize) + 1
                    }
                }
            }

            val posts = postApi.getPostList(
                groupId = 0,
                page = loadKey,
                loadSize = state.config.pageSize
            ).data ?: emptyList()

            postDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    postDb.dao.clearAll()
                }
                val postEntities = posts.map {
                    ListEntity.Post(
                        id = it.id,
                        userId = it.userId,
                        name = it.name,
                        text = it.text,
                        status = it.status,
                        profileImage = it.profileImage,
                        timeStamp = it.timeStamp,
                        replyCount = it.replyCount,
                        likeCount = it.likeCount,
                        reportCount = it.reportCount,
                        attachment = it.attachment
                    )
                }
                postDb.dao.insertAll(postEntities)
            }
            MediatorResult.Success(
                endOfPaginationReached = posts.isEmpty()
            )
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}*/

object PostApi {
    private val data = List(200) { i ->
        ListItem.Post(id = i)
    }

    fun getPostList(
        @Query("group_id") groupId: Int,
        @Query("page") page: Int,
        @Query("load_size") loadSize: Int
    ): List<ListItem.Post> {
        val list = data.subList(page * loadSize, (page * loadSize) + loadSize)
        Log.e("IDIS_TEST", "getPostList page: $page, loadSize: $loadSize listSize: ${list.size} ${list.map { it.id }}")
        return list
    }
}