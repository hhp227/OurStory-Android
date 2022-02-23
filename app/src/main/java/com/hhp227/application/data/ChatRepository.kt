package com.hhp227.application.data

import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.ChatRoomItem
import com.hhp227.application.dto.MessageItem
import com.hhp227.application.dto.Resource
import com.hhp227.application.dto.UserItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONException
import org.json.JSONObject

class ChatRepository {
    fun getChatList() = callbackFlow<Resource<List<ChatRoomItem>>> {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, URLs.URL_CHAT_ROOMS, null, { response ->
            if (!response.getBoolean("error")) {
                val chatRoomArray = response.getJSONArray("chat_rooms")
                val chatRooms = mutableListOf<ChatRoomItem>()

                for (i in 0 until chatRoomArray.length()) {
                    with(chatRoomArray.getJSONObject(i)) {
                        val chatRoom = ChatRoomItem(
                            id = getInt("chat_room_id"),
                            name = getString("name"),
                            timeStamp = getString("created_at")
                        )

                        chatRooms.add(chatRoom)
                        FirebaseMessaging.getInstance().subscribeToTopic("topic_${chatRoom.id}")
                    }
                }
                trySendBlocking(Resource.Success(chatRooms))
            }
        }, { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        })

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose { close() }
    }

    fun getChatMessages(chatRoomId: Int, offset: Int) = callbackFlow<Resource<List<MessageItem>>> {
        val strReq = StringRequest(Request.Method.GET, URLs.URL_CHAT_THREAD.replace("{CHATROOM_ID}", "$chatRoomId").replace("{OFFSET}", offset.toString()), { response ->
            try {
                val obj = JSONObject(response)
                val list = mutableListOf<MessageItem>()

                // check for error
                if (!obj.getBoolean("error")) {
                    val commentsObj = obj.getJSONArray("messages")

                    for (i in 0 until commentsObj.length()) {
                        val commentObj = commentsObj[i] as JSONObject
                        val commentId = commentObj.getInt("message_id")
                        val commentText = commentObj.getString("message")
                        val createdAt = commentObj.getString("created_at")
                        val userObj = commentObj.getJSONObject("user")
                        val userId = userObj.getInt("user_id")
                        val userName = userObj.getString("username")
                        val profileImage = userObj.getString("profile_img")
                        val message = MessageItem(
                            commentId,
                            commentText,
                            createdAt,
                            UserItem(userId, userName, null, "", profileImage, null)
                        )

                        list.add(message)
                    }
                } else {
                    trySendBlocking(Resource.Error(obj.getString("message")))
                }
            } catch (e: JSONException) {
                e.message?.let { trySendBlocking(Resource.Error(it)) }
            }
        }) { error ->
            trySendBlocking(Resource.Error("Volley error: " + error.message + ", code: " + error.networkResponse))
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(strReq)
        awaitClose { close() }
    }
}