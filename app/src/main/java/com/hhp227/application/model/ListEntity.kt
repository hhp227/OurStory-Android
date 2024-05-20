package com.hhp227.application.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

/*sealed class ListEntity {

    @Entity
    data class Post(
        @PrimaryKey var id: Int = 0,
        var userId: Int = 0,
        var name: String? = null,
        var text: String = "",
        var status: String? = null,
        var profileImage: String? = null,
        var timeStamp: String? = null,
        var replyCount: Int = 0,
        var likeCount: Int = 0,
        var reportCount: Int = 0,
        var attachment: Attachment = Attachment()
    ) : ListEntity()

    @Entity
    data class Image(
        @PrimaryKey var id: Int = 0,
        var image: String? = null,
        var tag: String? = null,
        var bitmap: Bitmap? = null
    ) : ListEntity()

    @Entity
    data class Attachment(
        var imageItemList: List<Image> = emptyList(),
        var video: String? = null
    ) : ListEntity()

}*/