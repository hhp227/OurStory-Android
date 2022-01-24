package com.hhp227.application.dto

sealed class GroupItem {
    data class Title(var text: String) : GroupItem()

    data class Ad(var text: String) : GroupItem()

    data class Group constructor(
        var id: Int = 0,
        var authorId: Int = 0,
        var groupName: String? = null,
        var authorName: String? = null,
        var image: String? = null,
        var description: String? = null,
        var createdAt: String? = null,
        var joinType: Int = 0
    ) : GroupItem()

    data class Empty(var res: Int, var text: String) : GroupItem()
}