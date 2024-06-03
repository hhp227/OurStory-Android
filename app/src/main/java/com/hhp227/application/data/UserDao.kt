package com.hhp227.application.data

import com.hhp227.application.model.User
import java.util.concurrent.ConcurrentHashMap

object UserDao {
    private val cachedMap = ConcurrentHashMap<Int, MutableList<User>>()

    private val countMap = ConcurrentHashMap<Int, Int>()

    fun insertAll(key: Int, list: List<User>) {
        countMap[key] = 0
        cachedMap.computeIfAbsent(key, ::ArrayList).addAll(list)
    }

    fun getUserList(key: Int, start: Int, end: Int): List<User> {
        return cachedMap[key]?.let { list ->
            list.slice(start until if (end < list.size) end else list.size)
        } ?: emptyList()
    }

    fun deleteUser(postId: Int) {
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