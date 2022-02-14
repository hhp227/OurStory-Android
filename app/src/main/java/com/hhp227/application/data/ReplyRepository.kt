package com.hhp227.application.data

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.ReplyItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONException
import org.json.JSONObject

class ReplyRepository {
    fun getReply(apiKey: String, replyId: Int) = callbackFlow<Resource<ReplyItem>> {
        val stringRequest = object : StringRequest(Method.GET, URLs.URL_REPLY.replace("{REPLY_ID}", replyId.toString()), Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {
                    val reply = ReplyItem.Reply(
                        id = jsonObject.getInt("id"),
                        userId = jsonObject.getInt("user_id"),
                        name = jsonObject.getString("name"),
                        reply = jsonObject.getString("reply"),
                        profileImage = jsonObject.getString("profile_img"),
                        timeStamp = jsonObject.getString("created_at")
                    )

                    trySendBlocking(Resource.Success(reply))
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

    fun addReply(apiKey: String, postId: Int, text: String) = callbackFlow<Resource<Int>> {
        val tagStringReq = "req_send"
        val stringRequest = object : StringRequest(Method.POST, URLs.URL_REPLYS.replace("{POST_ID}", postId.toString()), Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)

                // jsonObject ex) {"error":false,"message":"Reply created successfully","post_id":"1256","reply_id":921,"reply":"테스트"}
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
}