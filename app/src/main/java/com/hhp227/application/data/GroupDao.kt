package com.hhp227.application.data

import com.hhp227.application.model.GroupItem
import java.util.concurrent.ConcurrentHashMap

object GroupDao {
    private val cachedMap = ConcurrentHashMap<Int, MutableList<GroupItem>>()

    private val countMap = ConcurrentHashMap<Int, Int>()

    fun insertAll(key: Int, list: List<GroupItem>) {
        cachedMap.computeIfAbsent(key, ::ArrayList).addAll(list)
        countMap[key] = 0
    }

    fun getGroupList(key: Int, start: Int, end: Int): List<GroupItem.Group> {
        return cachedMap[key]?.let { list ->
            list.slice(start until if (end < list.size) end else list.size).map { it as GroupItem.Group }
        } ?: emptyList()
    }

    fun deleteGroup(groupId: Int) {
        val key = cachedMap.entries.find { it.value.find { (it as? GroupItem.Group)?.id == groupId } != null }?.key
        val index = cachedMap[key]?.indexOfFirst { (it as? GroupItem.Group)?.id == groupId } ?: -1

        countMap[key]?.minus(1)
        if (index > -1) {
            cachedMap[key]?.removeAt(index)
        }
    }

    fun deleteAll(key: Int) {
        countMap[key] = 0
        cachedMap[key]?.clear()
    }

    fun getCount(key: Int) = countMap[key] ?: 0
}