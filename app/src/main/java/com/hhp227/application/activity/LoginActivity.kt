package com.hhp227.application.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.R
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.User
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login);

        // 로그인 버튼 클릭 이벤트
        bLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 폼에 데이터가 비어있는지 확인
            if (email.isNotEmpty() && password.isNotEmpty()) {

                // 태그는 요청을 취소할때 사용
                val tagStringReq = "req_login"
                val stringRequest = object : StringRequest(Method.POST, URLs.URL_LOGIN, Response.Listener { response ->
                    hideProgressBar()
                    try {
                        JSONObject(response).takeUnless { it.getBoolean("error") }?.let {
                            val userId = it.getInt("id")
                            val userName = it.getString("name")
                            val userEmail = it.getString("email")
                            val apiKey = it.getString("api_key")
                            val profileImg = it.getString("profile_img")
                            val createdAt = it.getString("created_at")

                            User(userId, userName, userEmail, apiKey, profileImg, createdAt)
                        }.also {
                            AppController.getInstance().preferenceManager.storeUser(it)
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "JSON에러$e")
                        Toast.makeText(this, "로그인 실패", Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    error.message?.let {
                        Log.e(TAG, it)
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                    }
                    hideProgressBar()
                }) {
                    override fun getParams() = mapOf("email" to email, "password" to password)
                }

                showProgressBar()
                AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
            } else
                Toast.makeText(this, getString(R.string.login_input_correct), Toast.LENGTH_LONG).show()
        }

        // 가입하기 클릭 이벤트
        tvRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
    }

    private fun showProgressBar() = progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    companion object {
        private val TAG = LoginActivity::class.simpleName
    }
}