package com.hhp227.application.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.app.AppController
import com.hhp227.application.data.UserRepository
import com.hhp227.application.databinding.ActivityRegisterBinding
import com.hhp227.application.viewmodel.RegisterViewModel
import com.hhp227.application.viewmodel.RegisterViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class RegisterActivity : AppCompatActivity() {
    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory(UserRepository())
    }

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
            val passwordCheck = binding.etPasswordCheck.text.toString().trim()

            viewModel.register(name, email, password, passwordCheck)
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.registerFormState != null -> {
                    state.registerFormState.nameError?.let { error -> binding.etName.error = getString(error) }
                    state.registerFormState.emailError?.let { error -> binding.etEmail.error = getString(error) }
                    state.registerFormState.passwordError?.let { error -> binding.etPassword.error = getString(error) }
                    state.registerFormState.passwordCheckError?.let { error -> binding.etPasswordCheck.error = getString(error) }
                }
                state.error?.isBlank() ?: false -> {
                    hideProgressBar()
                    Toast.makeText(this, getString(R.string.register_complete), Toast.LENGTH_LONG).show()
                    finish()
                }
                state.error?.isNotBlank() ?: false -> {
                    hideProgressBar()
                    currentFocus?.let { Snackbar.make(it, state.error ?: "An unexpected error occured", Snackbar.LENGTH_LONG).show() }
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun showProgressBar() {
        if (binding.progressBar.visibility == View.GONE)
            binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        if (binding.progressBar.visibility == View.VISIBLE)
            binding.progressBar.visibility = View.GONE
    }
}