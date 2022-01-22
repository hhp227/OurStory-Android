package com.hhp227.application.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hhp227.application.app.AppController
import com.hhp227.application.databinding.ActivityLoginBinding
import com.hhp227.application.util.Resource
import com.hhp227.application.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels()

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)
        viewModel.resource.observe(this) {
            when (it) {
                is Resource.Success -> {
                    hideProgressBar()
                    AppController.getInstance().preferenceManager.storeUser(it.data)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is Resource.Error -> {
                    hideProgressBar()
                    it.message?.let { it1 -> Log.e(TAG, it1) }
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }

        // 로그인 버튼 클릭 이벤트
        binding.bLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            viewModel.login(email, password)
        }

        // 가입하기 클릭 이벤트
        binding.tvRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    companion object {
        private val TAG = LoginActivity::class.simpleName
    }
}