package com.hhp227.application.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.model.ListItem
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

/*import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hhp227.application.model.ListEntity

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<ListEntity.Post>)

    @Query("SELECT * FROM posts ORDER BY id ASC") // @Query("SELECT * FROM posts WHERE group_id = :group_io ORDER BY _id ASC")
    fun postsByGroupId(): PagingSource<Int, ListEntity.Post>

    @Query("DELETE FROM posts")
    suspend fun clearAll()

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deleteByPostId(id: String)
}*/

// Post Caching
object PostDao {
    val cachedPostList = mutableListOf<ListItem.Post>()

    var index = 0

    fun insertAll(list: List<ListItem.Post>) {
        cachedPostList.addAll(list)
    }

    fun getPostList(start: Int, end: Int): List<ListItem.Post> {
        return cachedPostList.slice(start until if (end < cachedPostList.size) end else cachedPostList.size)
    }

    fun deletePost(postId: Int) {
        index--
        cachedPostList.filter { it.id != postId }
    }

    fun clear() {
        cachedPostList.clear()
    }

    fun getPagingSource(groupId: Int, postService: PostApi): PagingSource<Int, ListItem.Post> {
        return object : PagingSource<Int, ListItem.Post>() {
            override fun getRefreshKey(state: PagingState<Int, ListItem.Post>): Int? {
                return state.anchorPosition?.let { anchorPosition ->
                    state.closestPageToPosition(anchorPosition)?.prevKey
                }
            }

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListItem.Post> {
                return try {
                    val nextPage: Int = params.key ?: 0
                    val loadSize: Int = params.loadSize
                    val data = postService.getPostList(groupId, nextPage, loadSize)

                    cachedPostList.addAll(data)
                    Log.e("IDIS_TEST", "nextPage: $nextPage, tempList: ${cachedPostList.map { it.id }}")
                    delay(2000)
                    LoadResult.Page(
                        data = cachedPostList.slice(nextPage * loadSize until if ((nextPage * loadSize) + loadSize < cachedPostList.size) (nextPage * loadSize) + loadSize else cachedPostList.size),
                        prevKey = if (nextPage == 0) null else nextPage - 1,
                        nextKey = if (params.key == null) nextPage + 3 else nextPage + 1
                    )
                } catch (e: IOException) {
                    Log.e("IDIS_TEST", "error: message: ${e.message}")
                    LoadResult.Error(e)
                } catch (e: HttpException) {
                    LoadResult.Error(e)
                }
            }
        }
    }
}