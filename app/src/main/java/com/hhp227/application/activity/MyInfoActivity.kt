package com.hhp227.application.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hhp227.application.R
import com.hhp227.application.databinding.ActivityTabsBinding
import com.hhp227.application.fragment.MyInfoFragment
import com.hhp227.application.fragment.MyPostFragment

class MyInfoActivity : AppCompatActivity() {
    private lateinit var tabLayoutMediator: TabLayoutMediator

    lateinit var binding: ActivityTabsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabsBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(toolbar)
            viewPager.apply {
                val fragments = listOf(MyInfoFragment.newInstance(), MyPostFragment.newInstance())
                adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
                    override fun getItemCount() = fragments.size

                    override fun createFragment(position: Int) = fragments[position]
                }
                offscreenPageLimit = fragments.size
            }
        }
        tabLayoutMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = resources.getStringArray(R.array.tab_myinfo)[position]
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tabLayoutMediator.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (tabLayoutMediator.isAttached) {
            tabLayoutMediator.detach()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}