package com.hhp227.application.data

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
import kotlinx.coroutines.flow.callbackFlow
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

    fun getReply(apiKey: String?, replyId: Int) = callbackFlow<Resource<ListItem>> {
        val stringRequest = object : StringRequest(Method.GET, URLs.URL_REPLY.replace("{REPLY_ID}", replyId.toString()), Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {
                    trySendBlocking(Resource.Success(parseReply(jsonObject)))
                }
            } catch (e: JSONException) {
                trySendBlocking(Resource.Error(e.message.toString()))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(stringRequest)
        awaitClose { close() }
    }

    fun addReply(apiKey: String?, postId: Int, text: String) = callbackFlow<Resource<Int>> {
        val tagStringReq = "req_send"
        val stringRequest = object : StringRequest(Method.POST, URLs.URL_REPLYS.replace("{POST_ID}", postId.toString()), Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {
                    trySendBlocking(Resource.Success(jsonObject.getInt("reply_id")))
                }
            } catch (e: JSONException) {
                trySendBlocking(Resource.Error(e.message.toString()))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)

            override fun getParams() = mapOf("reply" to text)
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
        awaitClose { close() }
    }

    fun setReply(apiKey: String, replyId: Int, text: String) = callbackFlow<Resource<String>> {
        val tagStringReq = "req_send"
        val stringRequest = object : StringRequest(Method.PUT, URLs.URL_REPLY.replace("{REPLY_ID}", replyId.toString()), Response.Listener { response ->
            val jsonObject = JSONObject(response)

            if (!jsonObject.getBoolean("error")) {
                trySendBlocking(Resource.Success(text))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)

            override fun getParams() = mapOf("reply" to text, "status" to "0")
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
        awaitClose { close() }
    }

    // 지독하게 안된다 ㅅㅂ
    /*fun setReply(apiKey: String, replyId: Int, text: String): Flow<Resource<String?>> = flow {
        emit(Resource.Loading())
        try {
            val response = replyService.setReply(apiKey, replyId.toString(), text)

            Log.e("TEST", "setReply: $response")
            emit(Resource.Success(text))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage, null))
        }
    }*/

    fun removeReply(apiKey: String?, replyId: Int) = callbackFlow<Resource<Boolean>> {
        val tagStringReq = "req_delete"
        val stringRequest = object : StringRequest(Method.DELETE, URLs.URL_REPLY.replace("{REPLY_ID}", replyId.toString()), Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {
                    trySendBlocking(Resource.Success(true))
                }
            } catch (e: JSONException) {
                trySendBlocking(Resource.Error(e.message.toString()))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = hashMapOf("Authorization" to apiKey)
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
        awaitClose { close() }
    }

    companion object {
        @Volatile private var instance: ReplyRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: ReplyRepository(ReplyService.create()).also { instance = it }
            }
    }
}