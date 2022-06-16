package com.hhp227.application.dto

import android.graphics.Bitmap
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

sealed class ListItem {
    data class Empty(var res: Int, var text: String) : ListItem()

    @Parcelize
    data class Post(
        @SerializedName("id") var id: Int = 0,
        @SerializedName("user_id") var userId: Int = 0,
        @SerializedName("name") var name: String? = null,
        @SerializedName("text") var text: String? = null,
        @SerializedName("status") var status: String? = null,
        @SerializedName("profile_img") var profileImage: String? = null,
        @SerializedName("created_at") var timeStamp: String? = null,
        @SerializedName("reply_count") var replyCount: Int = 0,
        @SerializedName("like_count") var likeCount: Int = 0,
        @SerializedName("report_count") var reportCount: Int = 0,
        @SerializedName("attachment") var attachment: Attachment = Attachment()
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

    @Parcelize
    data class Attachment(
        @SerializedName("images") var imageItemList: List<Image> = emptyList(),
        @SerializedName("video") var video: String? = null
    ) : Parcelable, ListItem()

    object Loader : ListItem() // 임시 코드
}