package com.hhp227.application.data

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.User
import com.hhp227.application.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONException
import org.json.JSONObject

class UserRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun login(email: String, password: String) = callbackFlow<Resource<User>> {

        // 태그는 요청을 취소할때 사용
        val tagStringReq = "req_login"
        val stringRequest = object : StringRequest(Method.POST, URLs.URL_LOGIN, Response.Listener { response ->
            try {
                JSONObject(response).takeUnless { it.getBoolean("error") }?.let {
                    val userId = it.getInt("id")
                    val userName = it.getString("name")
                    val userEmail = it.getString("email")
                    val apiKey = it.getString("api_key")
                    val profileImg = it.getString("profile_img")
                    val createdAt = it.getString("created_at")

                    User(userId, userName, userEmail, apiKey, profileImg, createdAt)
                }?.also {
                    sendBlocking(Resource.Success(it))
                }
            } catch (e: JSONException) {
                e.message?.let { 
                    sendBlocking(Resource.Error(it))
                }
            }
        }, Response.ErrorListener { error ->
            error.message?.let {
                sendBlocking(Resource.Error(it))
            }
        }) {
            override fun getParams() = mapOf("email" to email, "password" to password)
        }

        sendBlocking(Resource.Loading())
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
        awaitClose { close() }
    }

    fun register(name: String, email: String, password: String) = callbackFlow<Resource<Unit>> {
        val tagStringReq = "req_register"
        val stringRequest: StringRequest = object : StringRequest(Method.POST, URLs.URL_REGISTER, Response.Listener { response ->
            try {
                JSONObject(response).let {
                    val error = it.getBoolean("error")

                    if (!error) {
                        sendBlocking(Resource.Success(Unit))
                    } else {
                        val errorMsg = it.getString("message")

                        sendBlocking(Resource.Error(errorMsg))
                    }
                }
            } catch (e: JSONException) {
                e.message?.let {
                    sendBlocking(Resource.Error(it))
                }
            }
        }, Response.ErrorListener { error ->
            error.message?.let {
                sendBlocking(Resource.Error(it))
            }
        }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["name"] = name
                params["email"] = email
                params["password"] = password
                return params
            }
        }

        sendBlocking(Resource.Loading())
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
        awaitClose { close() }
    }
}