package com.hhp227.application.dto

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ListItem {
    data class Empty(var res: Int, var text: String) : ListItem()

    @Parcelize
    data class Post(
        var id: Int = 0,
        var userId: Int = 0,
        var name: String? = null,
        var text: String? = null,
        var imageItemList: List<Image> = listOf(),
        var status: String? = null,
        var profileImage: String? = null,
        var timeStamp: String? = null,
        var replyCount: Int = 0,
        var likeCount: Int = 0
    ) : Parcelable, ListItem()

    @Parcelize
    data class Reply(
        var id: Int = 0,
        var userId: Int = 0,
        var name: String? = null,
        var profileImage: String? = null,
        var timeStamp: String? = null,
        var reply: String? = null
    ) : Parcelable, ListItem()

    @Parcelize
    data class Image(
        var id: Int = 0,
        var image: String? = null,
        var tag: String? = null,
        var bitmap: Bitmap? = null
    ) : Parcelable, ListItem()

    object Loader : ListItem() // 임시 코드
}