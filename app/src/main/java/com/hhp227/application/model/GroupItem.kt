package com.hhp227.application.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class GroupItem {
    data class Title(var resId: Int) : GroupItem()

    data class Ad(var text: String) : GroupItem()

    @Serializable
    @Parcelize
    data class Group(
        @SerialName("id") var id: Int = 0,
        @SerialName("author_id") var authorId: Int = 0,
        @SerialName("name") var groupName: String? = null,
        @SerialName("author_name") var authorName: String? = null,
        @SerialName("image") var image: String? = null,
        @SerialName("description") var description: String? = null,
        @SerialName("created_at") var createdAt: String? = null,
        @SerialName("join_type") var joinType: Int = 0
    ) : GroupItem(), Parcelable

    data class Empty(var res: Int, var strRes: Int) : GroupItem()
}