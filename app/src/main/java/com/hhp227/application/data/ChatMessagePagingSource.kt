package com.hhp227.application.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hhp227.application.api.ChatService
import com.hhp227.application.model.ChatItem
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.max

class ChatMessagePagingSource(
    private val chatService: ChatService,
    private val chatDao: ChatDao,
    private val chatRoomId: Int
) : PagingSource<Int, ChatItem.Message>() {
    override fun getRefreshKey(state: PagingState<Int, ChatItem.Message>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            if (chatDao.isCacheEmpty(chatRoomId)) null
            else state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChatItem.Message> {
        return try {
            val offset: Int = params.key ?: 0
            val loadSize: Int = params.loadSize
            val key = max(0, offset + chatDao.getCount(chatRoomId))
            val nextKey = key + loadSize
            val prevKey = key - loadSize
            val response = chatService.getChatMessageList(chatRoomId, key, loadSize)

            if (offset == 0) chatDao.deleteAll(chatRoomId)
            if (!response.error) {
                val data = response.data?.messageList?.reversed() ?: emptyList()

                chatDao.insertAll(chatRoomId, data)
                LoadResult.Page(
                    data = chatDao.getChatMessageList(chatRoomId, key, nextKey),
                    prevKey = if (data.isEmpty()) null else nextKey, // TODO 바꿔줄 필요있음
                    nextKey = if (offset == 0) null else prevKey // TODO
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
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}