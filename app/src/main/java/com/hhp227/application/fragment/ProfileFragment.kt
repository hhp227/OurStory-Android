package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hhp227.application.R
import com.hhp227.application.databinding.FragmentTabsBinding
import com.hhp227.application.util.autoCleared

// WIP
class ProfileFragment : Fragment() {
    private lateinit var tabLayoutMediator: TabLayoutMediator

    private var binding: FragmentTabsBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabLayoutMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = resources.getStringArray(R.array.tab_myinfo)[position]
        }

        binding.toolbar.setupWithNavController(findNavController())
        binding.viewPager.apply {
            val fragments = listOf(MyInfoFragment.newInstance(), MyPostFragment.newInstance())
            adapter = object : FragmentStateAdapter(this@ProfileFragment) {
                override fun getItemCount() = fragments.size

                override fun createFragment(position: Int) = fragments[position]
            }
            offscreenPageLimit = fragments.size
        }
        tabLayoutMediator.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (tabLayoutMediator.isAttached) {
            tabLayoutMediator.detach()
        }
    }

    fun inflateMenu(action: () -> Unit) {
        if (binding.toolbar.menu.isEmpty()) {
            binding.toolbar.inflateMenu(R.menu.save)
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.save -> {
                    action.invoke()
                    true
                }
                else -> false
            }
        }
    }
}