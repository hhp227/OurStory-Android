package com.hhp227.application.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.ActivityRegisterBinding
import com.hhp227.application.viewmodel.RegisterViewModel
import org.json.JSONException
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {
    private val viewModel: RegisterViewModel by viewModels()

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)
        if (AppController.getInstance().preferenceManager.user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        binding.bRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

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
        if (binding.progressBar.visibility == View.GONE)
            binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        if (binding.progressBar.visibility == View.VISIBLE)
            binding.progressBar.visibility = View.GONE
    }

    companion object {
        private val TAG: String? = RegisterActivity::class.simpleName
    }
}