package com.hhp227.application.data

import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.ChatRoomItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow

class ChatRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
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

        trySendBlocking(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose { close() }
    }
}