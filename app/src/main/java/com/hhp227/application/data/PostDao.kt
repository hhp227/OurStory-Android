package com.hhp227.application.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.model.ListItem
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

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
    private val cachedMap = ConcurrentHashMap<Int, MutableList<ListItem.Post>>()

    var count = 0

    fun insertAll(key: Int, list: List<ListItem.Post>) {
        resetCount()
        cachedMap.computeIfAbsent(key, ::ArrayList).addAll(list)
    }

    fun getPostList(key: Int, start: Int, end: Int): List<ListItem.Post> {
        return cachedMap[key]?.let { list ->
            list.slice(start until if (end < list.size) end else list.size)
        } ?: emptyList()
    }

    fun deletePost(postId: Int) {
        val key = cachedMap.entries.find { it.value.find { it.id == postId } != null }?.key
        val index = cachedMap[key]?.indexOfFirst { it.id == postId } ?: -1
        count--
        if (index > -1) {
            cachedMap[key]?.removeAt(index)
        }
    }

    fun deleteAll(key: Int) {
        resetCount()
        cachedMap[key]?.clear()
    }

    fun resetCount() {
        count = 0
    }

    fun isCacheEmpty(key: Int) = cachedMap.getOrDefault(key, emptyList()).isEmpty()

    fun cachedList(key: Int) = cachedMap[key]
}
