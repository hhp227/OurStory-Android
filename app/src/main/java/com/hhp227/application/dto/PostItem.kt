package com.hhp227.application.dto

data class PostItem(
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
)

data class EmptyItem(var res: Int, var text: String)