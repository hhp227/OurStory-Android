package com.hhp227.application.fragment

import Tab1Fragment.Companion.POST_INFO_CODE
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hhp227.application.*
import com.hhp227.application.activity.WriteActivity
import com.hhp227.application.activity.WriteActivity.Companion.TYPE_INSERT
import com.hhp227.application.databinding.FragmentTabHostLayoutBinding
import com.hhp227.application.fragment.GroupFragment.Companion.UPDATE_CODE
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.TabHostLayoutViewModel

class TabHostLayoutFragment : Fragment() {
    private val viewModel: TabHostLayoutViewModel by viewModels()

    private var binding: FragmentTabHostLayoutBinding by autoCleared()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.groupId = it.getInt("group_id")
            viewModel.authorId = it.getInt("author_id")
            viewModel.groupName = it.getString("group_name") ?: "Unknown Group"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabHostLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentList = arrayListOf(Tab1Fragment.newInstance(viewModel.groupId, viewModel.groupName), Tab2Fragment.newInstance(), Tab3Fragment.newInstance(viewModel.groupId), Tab4Fragment.newInstance(viewModel.groupId, viewModel.authorId))
        binding.collapsingToolbar.isTitleEnabled = false

        (requireActivity() as? AppCompatActivity)?.run {
            title = viewModel.groupName

            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@TabHostLayoutFragment) {
                override fun getItemCount() = fragmentList.size

                override fun createFragment(position: Int) = fragmentList[position]
            }
            offscreenPageLimit = fragmentList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = resources.getStringArray(R.array.tab_name)[position]
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.fab.visibility = if (tab?.position != 0) View.GONE else View.VISIBLE
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })
        binding.fab.setOnClickListener {
            Intent(context, WriteActivity::class.java).also { intent ->
                intent.putExtra("type", TYPE_INSERT)
                intent.putExtra("group_id", viewModel.groupId)
                startActivityForResult(intent, UPDATE_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        childFragmentManager.fragments.forEach { fragment -> fragment.onActivityResult(requestCode, resultCode, data) }
        if ((requestCode == UPDATE_CODE || requestCode == POST_INFO_CODE) && resultCode == RESULT_OK)
            binding.appBarLayout.setExpanded(true)
    }

    companion object {
        fun newInstance(groupId: Int, authorId: Int, groupName: String?): Fragment = TabHostLayoutFragment().apply {
            arguments = Bundle().apply {
                putInt("group_id", groupId)
                putInt("author_id", authorId)
                putString("group_name", groupName)
            }
        }
    }
}