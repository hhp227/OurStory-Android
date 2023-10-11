package com.hhp227.application.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class ChatItem {
    @Serializable
    @Parcelize
    data class ChatRoom(
        @SerialName("chat_room_id") var id: Int = 0,
        @SerialName("name") var name: String? = null,
        @SerialName("created_at") var timeStamp: String? = null
    ) : Parcelable, ChatItem()

    @Serializable
    @Parcelize
    data class Message(
        @SerialName("message_id") var id: Int = 0,
        @SerialName("chat_room_id") var chatRoomId: Int = 0,
        @SerialName("message") var message: String? = null,
        @SerialName("created_at") var time: String? = null,
        @SerialName("user") var user: User? = null
    ) : Parcelable, ChatItem()

    @Serializable
    @Parcelize
    data class MessageInfo(
        @SerialName("messages") val messageList: List<Message>,
        @SerialName("chat_room") val chatRoom: ChatRoom
    ) : Parcelable, ChatItem()
}