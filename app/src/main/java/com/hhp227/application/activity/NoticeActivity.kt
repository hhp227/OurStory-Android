package com.hhp227.application.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hhp227.application.databinding.ActivityNoticeBinding

class NoticeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNoticeBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}