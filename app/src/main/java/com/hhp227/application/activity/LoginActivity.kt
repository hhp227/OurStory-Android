package com.hhp227.application.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.app.AppController
import com.hhp227.application.databinding.ActivityLoginBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels {
        InjectorUtils.provideLoginViewModelFactory()
    }

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
                state.loginFormState != null -> {
                    state.loginFormState.emailError?.let { error -> binding.etEmail.error = getString(error) }
                    state.loginFormState.passwordError?.let { error -> binding.etPassword.error = getString(error) }
                }
                state.user != null -> {
                    hideProgressBar()
                    AppController.getInstance().preferenceManager.storeUser(state.user)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    currentFocus?.let { Snackbar.make(it, state.error, Snackbar.LENGTH_LONG).show() }
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }
}