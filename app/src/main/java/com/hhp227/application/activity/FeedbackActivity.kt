package com.hhp227.application.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hhp227.application.databinding.ActivityFeedbackBinding

class FeedbackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}