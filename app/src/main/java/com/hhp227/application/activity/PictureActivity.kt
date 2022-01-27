package com.hhp227.application.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.hhp227.application.adapter.PicturePagerAdapter
import com.hhp227.application.databinding.ActivityPictureBinding

class PictureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPictureBinding

    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

        override fun onPageSelected(position: Int) {
            binding.tvCount.text = "${position + 1}/${binding.viewPager.adapter?.count}"
        }

        override fun onPageScrollStateChanged(state: Int) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPictureBinding.inflate(layoutInflater)
        val position = intent.getIntExtra("position", 0)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.viewPager.apply {
            adapter = PicturePagerAdapter(intent.getParcelableArrayListExtra("images") ?: emptyList())

            addOnPageChangeListener(onPageChangeListener)
            setCurrentItem(position, false)
        }
        binding.tvCount.apply {
            visibility = if (binding.viewPager.adapter?.count ?: 0 > 1) View.VISIBLE else View.GONE
            text = "${position + 1}/${binding.viewPager.adapter?.count}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewPager.removeOnPageChangeListener(onPageChangeListener)
    }
}