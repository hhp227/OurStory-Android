package com.hhp227.application.data

import com.hhp227.application.model.GroupItem

object GroupDao {
    private val cachedMap = mutableMapOf<Int, MutableList<GroupItem>>()

    var count = 0
}