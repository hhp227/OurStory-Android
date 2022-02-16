package com.hhp227.application.data

import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.UserItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class UserRepository {
    private fun parseUserList(jsonArray: JSONArray) = List(jsonArray.length()) { i -> parseUser(jsonArray.getJSONObject(i)) }

    private fun parseUser(jsonObject: JSONObject) = UserItem(
        id = jsonObject.getInt("id"),
        name = jsonObject.getString("name"),
        email = jsonObject.getString("email"),
        apiKey = try { jsonObject.getString("api_key") } catch (e: JSONException) { "null" },
        profileImage = jsonObject.getString("profile_img"),
        createAt = jsonObject.getString("created_at")
    )

    fun login(email: String, password: String) = callbackFlow<Resource<UserItem>> {

        // 태그는 요청을 취소할때 사용
        val tagStringReq = "req_login"
        val stringRequest = object : StringRequest(Method.POST, URLs.URL_LOGIN, Response.Listener { response ->
            try {
                JSONObject(response).takeUnless { it.getBoolean("error") }?.let(::parseUser)?.also {
                    trySendBlocking(Resource.Success(it))
                }
            } catch (e: JSONException) {
                e.message?.let { 
                    trySendBlocking(Resource.Error(it))
                }
            }
        }, Response.ErrorListener { error ->
            error.message?.let {
                trySendBlocking(Resource.Error(it))
            }
        }) {
            override fun getParams() = mapOf("email" to email, "password" to password)
        }

        trySend(Resource.Loading())
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
                        trySendBlocking(Resource.Success(Unit))
                    } else {
                        val errorMsg = it.getString("message")

                        trySendBlocking(Resource.Error(errorMsg))
                    }
                }
            } catch (e: JSONException) {
                e.message?.let {
                    trySendBlocking(Resource.Error(it))
                }
            }
        }, Response.ErrorListener { error ->
            error.message?.let {
                trySendBlocking(Resource.Error(it))
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

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
        awaitClose { close() }
    }

    fun getUserList(groupId: Int) = callbackFlow<Resource<List<UserItem>>> {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, "${URLs.URL_MEMBER}/${groupId}", null, { response ->
            trySendBlocking(Resource.Success(parseUserList(response.getJSONArray("users"))))
        }) { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose { close() }
    }
}