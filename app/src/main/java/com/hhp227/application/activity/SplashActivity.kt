package com.hhp227.application.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hhp227.application.R
import com.hhp227.application.app.AppController
import com.hhp227.application.databinding.ActivitySplashBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.viewmodel.SplashViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SplashActivity : AppCompatActivity() {
    private val viewModel: SplashViewModel by viewModels {
        InjectorUtils.provideSplashViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivitySplashBinding.inflate(layoutInflater).root)

        viewModel.userFlow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { user ->
            val intent = Intent(this, user?.let { MainActivity::class.java } ?: LoginActivity::class.java)

            delay(SPLASH_TIME_OUT)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.splash_in, R.anim.splash_out)
        }.launchIn(lifecycleScope)
    }

    companion object {
        private const val SPLASH_TIME_OUT = 1250L
    }
}