package com.hhp227.application.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.hhp227.application.R
import com.hhp227.application.databinding.ActivityTabsBinding
import com.hhp227.application.fragment.MyInfoFragment
import com.hhp227.application.fragment.MyPostFragment

class MyInfoActivity : AppCompatActivity() {
    lateinit var binding: ActivityTabsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabsBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(toolbar)
            resources.getStringArray(R.array.tab_myinfo).forEach { tabName ->
                tabLayout.addTab(tabLayout.newTab().setText(tabName))
            }
            tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
            viewPager.apply {
                val fragments = listOf(MyInfoFragment.newInstance(), MyPostFragment.newInstance())
                adapter = object : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
                    override fun getCount() = fragments.size

                    override fun getItem(position: Int): Fragment {
                        return fragments[position]
                    }
                }
                offscreenPageLimit = fragments.size

                addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}