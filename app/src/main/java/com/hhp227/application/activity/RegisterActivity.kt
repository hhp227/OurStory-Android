package com.hhp227.application.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hhp227.application.app.AppController
import com.hhp227.application.databinding.ActivityRegisterBinding
import com.hhp227.application.util.Resource
import com.hhp227.application.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {
    private val viewModel: RegisterViewModel by viewModels()

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)
        viewModel.resource.observe(this) {
            when (it) {
                is Resource.Success -> {
                    hideProgressBar()
                    Toast.makeText(this, "가입완료", Toast.LENGTH_LONG).show()
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