package com.hhp227.application.data

import com.hhp227.application.model.GroupItem
import java.util.concurrent.ConcurrentHashMap

object GroupDao {
    private val cachedMap = ConcurrentHashMap<Int, MutableList<GroupItem>>()

    var count = 0

    fun insertAll(key: Int, list: List<GroupItem>) {
        resetCount()
        cachedMap.computeIfAbsent(key, ::ArrayList).addAll(list)
    }

    fun getGroupList(key: Int, start: Int, end: Int): List<GroupItem.Group> {
        return cachedMap[key]?.let { list ->
            list.slice(start until if (end < list.size) end else list.size).map { it as GroupItem.Group }
        } ?: emptyList()
    }

    fun resetCount() {
        count = 0
    }
}