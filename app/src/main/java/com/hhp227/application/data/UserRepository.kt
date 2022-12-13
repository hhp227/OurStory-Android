package com.hhp227.application.data

import android.graphics.Bitmap
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.api.AuthService
import com.hhp227.application.app.AppController
import com.hhp227.application.util.URLs
import com.hhp227.application.model.Resource
import com.hhp227.application.model.User
import com.hhp227.application.volley.util.MultipartRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class UserRepository(private val authService: AuthService) {
    private fun parseUserList(jsonArray: JSONArray) = List(jsonArray.length()) { i -> parseUser(jsonArray.getJSONObject(i)) }

    private fun parseUser(jsonObject: JSONObject) = User(
        id = jsonObject.getInt("id"),
        name = jsonObject.getString("name"),
        email = jsonObject.getString("email"),
        apiKey = try { jsonObject.getString("api_key") } catch (e: JSONException) { "null" },
        profileImage = jsonObject.getString("profile_img"),
        createdAt = jsonObject.getString("created_at")
    )

    fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val response = authService.login(email, password)

            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage, null))
        }
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
        awaitClose(::close)
    }

    fun getUserList(groupId: Int) = callbackFlow<Resource<List<User>>> {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, "${URLs.URL_MEMBER}/${groupId}", null, { response ->
            trySendBlocking(Resource.Success(parseUserList(response.getJSONArray("users"))))
        }) { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose(::close)
    }

    fun addProfileImage(apiKey: String, bitmap: Bitmap) = callbackFlow<Resource<String>> {
        val multiPartRequest = object : MultipartRequest(Method.POST, URLs.URL_USER_PROFILE_IMAGE_UPLOAD, Response.Listener { response ->
            JSONObject(String(response.data)).also { jsonObject ->
                if (!jsonObject.getBoolean("error")) {
                    trySendBlocking(Resource.Success(jsonObject.getString("profile_img")))
                }
            }
        }, Response.ErrorListener {
            trySendBlocking(Resource.Error(it.message.toString()))
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)

            override fun getByteData() = mapOf(
                /**
                 *  프로필 이미지가 아이디 기준으로 일치 하지 않고 시간대로 해버리면 수정이 일어날때마다
                 *  모든 프로필 이미지가 포함된item들을 set해줘야함 추후 수정
                 */
                "profile_img" to DataPart("${System.currentTimeMillis()}.jpg", ByteArrayOutputStream().also {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
                }.toByteArray())
            )
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(multiPartRequest)
        awaitClose(::close)
    }

    fun setUserProfile(apiKey: String, imageUrl: String?) = callbackFlow<Resource<String>> {
        val stringRequest = object : StringRequest(Method.PUT, URLs.URL_PROFILE_EDIT, Response.Listener { response ->
            trySendBlocking(Resource.Success(imageUrl ?: "null"))
        }, Response.ErrorListener { error ->
            error.message?.let { trySendBlocking(Resource.Error(it)) }
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)

            override fun getParams() = mapOf(
                "profile_img" to imageUrl,
                "status" to "1"
            )
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(stringRequest)
        awaitClose(::close)
    }

    // TODO
    fun removeUser() {

    }

    fun isFriend(apiKey: String, friendId: Int) = callbackFlow<Resource<Int>> {
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, URLs.URL_USER_FRIEND.replace("{USER_ID}", friendId.toString()), null, Response.Listener { response ->
            if (!response.getBoolean("error")) {
                trySendBlocking(Resource.Success(response.getJSONObject("result").getInt("cnt")))
            } else {
                trySendBlocking(Resource.Error(response.getString("message")))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = hashMapOf("Authorization" to apiKey)
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose(::close)
    }

    fun getFriendList(apiKey: String, offset: Int) = callbackFlow<Resource<List<User>>> {
        val jsonArrayRequest = object : JsonArrayRequest(Method.GET, URLs.URL_USER_FRIENDS.replace("{OFFSET}", "$offset"), null, Response.Listener { response ->
            response?.let(::parseUserList)?.also { list ->
                trySendBlocking(Resource.Success(list))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonArrayRequest)
        awaitClose(::close)
    }

    fun toggleFriend(apiKey: String, friendId: Int) = callbackFlow<Resource<String>> {
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, URLs.URL_TOGGLE_FRIEND.replace("{USER_ID}", friendId.toString()), null, Response.Listener { response ->
            if (!response.getBoolean("error")) {
                trySendBlocking(Resource.Success(response.getString("result")))
            } else {
                trySendBlocking(Resource.Error(response.getString("message")))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = hashMapOf("Authorization" to apiKey)
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose(::close)
    }

    companion object {
        @Volatile private var instance: UserRepository? = null

        fun getInstance(authService: AuthService) =
            instance ?: synchronized(this) {
                instance ?: UserRepository(authService).also { instance = it }
            }
    }
}