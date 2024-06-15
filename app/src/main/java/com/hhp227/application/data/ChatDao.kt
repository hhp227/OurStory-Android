package com.hhp227.application.data

import com.hhp227.application.model.ChatItem
import java.util.concurrent.ConcurrentHashMap

object ChatDao {
    private val cachedMap = ConcurrentHashMap<Int, MutableList<ChatItem.Message>>()

    private val countMap = ConcurrentHashMap<Int, Int>()

    fun insertAll(key: Int, list: List<ChatItem.Message>) {
        countMap[key] = 0
        cachedMap.computeIfAbsent(key, ::ArrayList).addAll(list)
    }

    fun insert(key: Int, message: ChatItem.Message) {
        countMap.computeIfPresent(key) { _, v -> v + 1 }
        cachedMap.computeIfAbsent(key, ::ArrayList).add(message)
    }

    fun getChatMessageList(key: Int, start: Int, end: Int): List<ChatItem.Message> {
        return cachedMap[key]?.let { list ->
            list.slice(start until if (end < list.size) end else list.size)
        } ?: emptyList()
    }

    fun deleteChatMessage(messageId: Int) {
        val key = cachedMap.entries.find { it.value.find { it.id == messageId } != null }?.key ?: -1
        val index = cachedMap[key]?.indexOfFirst { it.id == messageId } ?: -1

        countMap.computeIfPresent(key) { _, v -> v - 1 }
        if (index > -1) {
            cachedMap[key]?.removeAt(index)
        }
    }

    fun deleteAll(key: Int) {
        countMap[key] = 0
        cachedMap[key]?.clear()
    }

    fun isCacheEmpty(key: Int) = cachedMap.getOrDefault(key, emptyList()).isEmpty()

    fun cachedList(key: Int) = cachedMap[key]

    fun getCount(key: Int) = countMap[key] ?: 0
}