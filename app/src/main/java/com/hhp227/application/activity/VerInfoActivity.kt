package com.hhp227.application.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.hhp227.application.BuildConfig
import com.hhp227.application.databinding.ActivityVerinfoBinding

class VerInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerinfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerinfoBinding.inflate(layoutInflater)
        binding.tvVerName.text = BuildConfig.VERSION_NAME

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.run { setDisplayHomeAsUpEnabled(true) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}