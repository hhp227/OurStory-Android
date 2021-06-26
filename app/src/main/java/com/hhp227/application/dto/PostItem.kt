package com.hhp227.application.dto

data class PostItem(
    var id: Int = 0,
    var userId: Int = 0,
    var name: String? = null,
    var text: String? = null,
    var imageItemList: MutableList<ImageItem> = mutableListOf(),
    var status: String? = null,
    var profileImage: String? = null,
    var timeStamp: String? = null,
    var replyCount: Int = 0,
    var likeCount: Int = 0
)
