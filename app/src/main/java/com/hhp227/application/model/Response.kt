package com.hhp227.application.model

data class GetPostListResponse(
    val error: Boolean,
    val posts: List<ListItem.Post>
    )

data class BasicApiResponse<T>(
    val successful: Boolean,
    val message: String? = null,
    val data: T? = null
)