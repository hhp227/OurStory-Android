package com.hhp227.application.data

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject

class ReplyRepository {
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