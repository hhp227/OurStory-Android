package com.hhp227.application.data

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.api.ReplyService
import com.hhp227.application.app.AppController
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.Resource
import com.hhp227.application.util.URLs
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import org.json.JSONException
import org.json.JSONObject

class ReplyRepository(private val replyService: ReplyService) {
    private fun parseReply(jsonObject: JSONObject): ListItem.Reply {
        return ListItem.Reply(
            id = jsonObject.getInt("id"),
            userId = jsonObject.getInt("user_id"),
            name = jsonObject.getString("name"),
            reply = jsonObject.getString("reply"),
            profileImage = jsonObject.getString("profile_img"),
            timeStamp = jsonObject.getString("created_at")
        )
    }

    fun getReplyList(apiKey: String?, postId: Int) = callbackFlow<Resource<List<ListItem>>> {
        val jsonArrayRequest = object : JsonArrayRequest(Method.GET, URLs.URL_REPLYS.replace("{POST_ID}", postId.toString()), null, Response.Listener { response ->
            try {
                trySendBlocking(Resource.Success(List(response.length()) { i -> parseReply(response.getJSONObject(i)) }))
            } catch (e: JSONException) {
                trySendBlocking(Resource.Error(e.message.toString()))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonArrayRequest)
        awaitClose { close() }
    }

    fun getReply(apiKey: String, replyId: Int): Flow<Resource<out ListItem.Reply>> = flow {
        try {
            val response = replyService.getReply(apiKey, replyId.toString())

            if (!response.error) {
                emit(Resource.Success(response.reply))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage, null))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun addReply(apiKey: String, postId: Int, text: String): Flow<Resource<out Int>> = flow {
        try {
            val response = replyService.addReply(apiKey, postId, text)

            if (!response.error) {
                emit(Resource.Success(response.replyId))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage, null))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun setReply(apiKey: String, replyId: Int, text: String): Flow<Resource<out String?>> = flow {
        try {
            val response = replyService.setReply(apiKey, replyId.toString(), text)

            if (!response.error) {
                emit(Resource.Success(text))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage, null))
        }
    }
        .onStart { emit(Resource.Loading()) }

    fun removeReply(apiKey: String, replyId: Int): Flow<Resource<out Boolean>> = flow {
        try {
            val response = replyService.removeReply(apiKey, replyId.toString())

            if (!response.error) {
                emit(Resource.Success(true))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage, null))
        }
    }
        .onStart { emit(Resource.Loading()) }

    companion object {
        @Volatile private var instance: ReplyRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: ReplyRepository(ReplyService.create()).also { instance = it }
            }
    }
}