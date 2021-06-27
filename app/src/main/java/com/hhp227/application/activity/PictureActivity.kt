package com.hhp227.application.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hhp227.application.databinding.ActivityPictureBinding

class PictureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPictureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPictureBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}