package com.hhp227.application.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ReplyItem {
    @Parcelize
    data class Post(
        var id: Int = 0,
        var userId: Int = 0,
        var name: String? = null,
        var text: String? = null,
        var imageItemList: List<ImageItem> = listOf(),
        var status: String? = null,
        var profileImage: String? = null,
        var timeStamp: String? = null,
        var replyCount: Int = 0,
        var likeCount: Int = 0
    ) : Parcelable, ReplyItem()

    @Parcelize
    data class Reply(
        var id: Int = 0,
        var userId: Int = 0,
        var name: String? = null,
        var profileImage: String? = null,
        var timeStamp: String? = null,
        var reply: String? = null
    ) : Parcelable, ReplyItem()
}