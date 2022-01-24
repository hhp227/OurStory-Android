package com.hhp227.application.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hhp227.application.app.AppController
import com.hhp227.application.databinding.ActivityRegisterBinding
import com.hhp227.application.viewmodel.RegisterViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class RegisterActivity : AppCompatActivity() {
    private val viewModel: RegisterViewModel by viewModels()

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.error?.isBlank() ?: false -> {
                    hideProgressBar()
                    Toast.makeText(this, "가입완료", Toast.LENGTH_LONG).show()
                    finish()
                }
                state.error?.isNotBlank() ?: false -> {
                    hideProgressBar()
                    Log.e(TAG, state.error ?: "")
                    Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
        if (AppController.getInstance().preferenceManager.user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        binding.bRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            viewModel.register(name, email, password)
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