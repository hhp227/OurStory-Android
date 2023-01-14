package com.hhp227.application.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPostListResponse(
    @SerialName("error") val error: Boolean,
    @SerialName("posts") val posts: List<ListItem.Post>
)

@Serializable
data class TogglePostLikeResponse(
    @SerialName("error") val error: Boolean,
    @SerialName("message") val message: String?,
    @SerialName("result") val result: String?
)

data class BasicApiResponse<T>(
    val successful: Boolean,
    val message: String? = null,
    val data: T? = null
)