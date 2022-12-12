package com.hhp227.application.dto

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class ListItem {
    data class Empty(var res: Int, var text: String) : ListItem()

    @Serializable
    @Parcelize
    data class Post(
        @SerialName("id") var id: Int = 0,
        @SerialName("user_id") var userId: Int = 0,
        @SerialName("name") var name: String? = null,
        @SerialName("text") var text: String = "",
        @SerialName("status") var status: String? = null,
        @SerialName("profile_img") var profileImage: String? = null,
        @SerialName("created_at") var timeStamp: String? = null,
        @SerialName("reply_count") var replyCount: Int = 0,
        @SerialName("like_count") var likeCount: Int = 0,
        @SerialName("report_count") var reportCount: Int = 0,
        @SerialName("attachment") var attachment: Attachment = Attachment()
    ) : Parcelable, ListItem()

    @Parcelize
    data class Reply(
        var id: Int = 0,
        var userId: Int = 0,
        var name: String? = null,
        var profileImage: String? = null,
        var timeStamp: String? = null,
        var reply: String = ""
    ) : Parcelable, ListItem()

    @Serializable
    @Parcelize
    data class Image(
        var id: Int = 0,
        var image: String? = null,
        var tag: String? = null,
        @Contextual var bitmap: Bitmap? = null
    ) : Parcelable, ListItem()

    @Serializable
    @Parcelize
    data class Attachment(
        @SerialName("images") var imageItemList: List<Image> = emptyList(),
        @SerialName("video") var video: String? = null
    ) : Parcelable, ListItem()

    object Loader : ListItem() // 임시 코드
}