package com.hhp227.application.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.hhp227.application.R
import com.hhp227.application.app.AppController

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AppController.getInstance().preferenceManager.user.let {
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, MainActivity::class.java)

                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.splash_in, R.anim.splash_out)
            }, SPLASH_TIME_OUT)
        } ?: startActivity(Intent(this, LoginActivity::class.java)).run { finish() }
    }

    companion object {
        private const val SPLASH_TIME_OUT = 1250L
    }
}