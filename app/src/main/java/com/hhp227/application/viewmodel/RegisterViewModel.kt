package com.hhp227.application.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.util.Resource
import org.json.JSONException
import org.json.JSONObject

class RegisterViewModel : ViewModel() {
    val resource = MediatorLiveData<Resource<Boolean>>()

    fun register(name: String, email: String, password: String) {
        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            val tagStringReq = "req_register"
            val stringRequest: StringRequest = object : StringRequest(Method.POST, URLs.URL_REGISTER, Response.Listener { response ->
                try {
                    JSONObject(response).let {
                        val error = it.getBoolean("error")

                        if (!error) {
                            resource.postValue(Resource.Success(error))
                        } else {
                            val errorMsg = it.getString("message")

                            resource.postValue(Resource.Error(errorMsg))
                        }
                    }
                } catch (e: JSONException) {
                    e.message?.let { resource.postValue(Resource.Error(it)) }
                }
            }, Response.ErrorListener { error ->
                error.message?.let {
                    resource.postValue(Resource.Error(it))
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

            AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
        } else
            resource.postValue(Resource.Error("입력값이 없습니다."))
    }
}