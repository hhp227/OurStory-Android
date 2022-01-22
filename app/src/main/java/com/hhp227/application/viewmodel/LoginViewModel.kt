package com.hhp227.application.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.User
import com.hhp227.application.util.Resource
import org.json.JSONException
import org.json.JSONObject

class LoginViewModel : ViewModel() {
    val resource = MediatorLiveData<Resource<User>>()

    fun login(email: String, password: String) {

        // 폼에 데이터가 비어있는지 확인
        if (email.isNotEmpty() && password.isNotEmpty()) {

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
                        resource.postValue(Resource.Success(it))
                    }
                } catch (e: JSONException) {
                    e.message?.let { resource.postValue(Resource.Error(it)) }
                }
            }, Response.ErrorListener { error ->
                error.message?.let {
                    resource.postValue(Resource.Error(it))
                }
            }) {
                override fun getParams() = mapOf("email" to email, "password" to password)
            }

            resource.postValue(Resource.Loading())
            AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
        } else
            resource.postValue(Resource.Error("login_input_correct"))
    }
}