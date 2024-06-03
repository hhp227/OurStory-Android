package com.hhp227.application.data

import com.hhp227.application.model.ListItem
import java.util.concurrent.ConcurrentHashMap

object AlbumDao {
    private val cachedMap = ConcurrentHashMap<Int, MutableList<ListItem.Post>>()

    private val countMap = ConcurrentHashMap<Int, Int>()

    fun insertAll(key: Int, list: List<ListItem.Post>) {
        countMap[key] = 0
        cachedMap.computeIfAbsent(key, ::ArrayList).addAll(list)
    }

    fun getPostList(key: Int, start: Int, end: Int): List<ListItem.Post> {
        return cachedMap[key]?.let { list ->
            list.slice(start until if (end < list.size) end else list.size)
        } ?: emptyList()
    }

    fun deletePost(postId: Int) {
        val key = cachedMap.entries.find { it.value.find { it.id == postId } != null }?.key ?: -1
        val index = cachedMap[key]?.indexOfFirst { it.id == postId } ?: -1

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