package com.hhp227.application.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetGroupListResponse(
    @SerialName("error") val error: Boolean,
    @SerialName("groups") val groups: List<GroupItem.Group>
)

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

@Serializable
data class GetUserListResponse(
    @SerialName("users") val users: List<User>
)

@Serializable
data class AddReplyResponse(
    @SerialName("error") val error: Boolean,
    @SerialName("message") val message: String?,
    @SerialName("post_id") val postId: String,
    @SerialName("reply_id") val replyId: Int,
    @SerialName("reply") val reply: String
)

@Serializable
data class GetReplyResponse(
    @SerialName("error") val error: Boolean,
    @SerialName("reply") val reply: ListItem.Reply
)


@Serializable
data class BasicApiResponse<T>(
    @SerialName("error") val error: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: T? = null
)