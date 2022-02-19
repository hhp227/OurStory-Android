package com.hhp227.application.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class GroupItem {
    object Title : GroupItem()

    data class Ad(var text: String) : GroupItem()

    @Parcelize
    data class Group constructor(
        var id: Int = 0,
        var authorId: Int = 0,
        var groupName: String? = null,
        var authorName: String? = null,
        var image: String? = null,
        var description: String? = null,
        var createdAt: String? = null,
        var joinType: Int = 0
    ) : GroupItem(), Parcelable

    data class Empty(var res: Int, var strRes: Int) : GroupItem()
}