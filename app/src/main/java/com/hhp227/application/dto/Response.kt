package com.hhp227.application.dto

data class GetPostListResponse(
    val error: Boolean,
    val posts: List<ListItem.Post>
    )