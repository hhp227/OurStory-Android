package com.hhp227.application.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.R
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONException
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {
    companion object {
        private val TAG: String? = RegisterActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        if (AppController.getInstance().preferenceManager.user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        bRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                val tagStringReq = "req_register"
                val stringRequest: StringRequest = object : StringRequest(Method.POST, URLs.URL_REGISTER, Response.Listener { response ->
                    hideProgressBar()
                    try {
                        JSONObject(response).let {
                            val error = it.getBoolean("error")

                            if (!error) {
                                Toast.makeText(this, "가입완료", Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                val errorMsg = it.getString("message")

                                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: JSONException) {
                        e.message?.let { Log.e(TAG, it) }
                    }
                }, Response.ErrorListener { error ->
                    error.message?.let {
                        Log.e(TAG, it)
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                    }
                    hideProgressBar()
                }) {
                    override fun getParams(): MutableMap<String, String> {
                        val params = HashMap<String, String>()
                        params["name"] = name
                        params["email"] = email
                        params["password"] = password
                        return params
                    }
                }

                showProgressBar()
                AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
            } else
                Toast.makeText(this, "입력값이 없습니다.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showProgressBar() {
        if (progressBar.visibility == View.GONE)
            progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        if (progressBar.visibility == View.VISIBLE)
            progressBar.visibility = View.GONE
    }
}