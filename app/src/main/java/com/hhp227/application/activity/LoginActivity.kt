package com.hhp227.application.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hhp227.application.app.AppController
import com.hhp227.application.data.UserRepository
import com.hhp227.application.databinding.ActivityLoginBinding
import com.hhp227.application.viewmodel.LoginViewModel
import com.hhp227.application.viewmodel.LoginViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(UserRepository())
    }

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // 로그인 버튼 클릭 이벤트
        binding.bLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            viewModel.login(email, password)
        }

        // 가입하기 클릭 이벤트
        binding.tvRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.user != null -> {
                    hideProgressBar()
                    AppController.getInstance().preferenceManager.storeUser(state.user)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    Log.e(TAG, state.error)
                    Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    companion object {
        private val TAG = LoginActivity::class.simpleName
    }
}