package com.hhp227.application.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hhp227.application.*
import com.hhp227.application.activity.CreatePostActivity
import com.hhp227.application.databinding.FragmentTabHostLayoutBinding
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.TabHostLayoutViewModel
import com.hhp227.application.viewmodel.TabHostLayoutViewModelFactory
import com.hhp227.application.viewmodel.CreatePostViewModel.Companion.TYPE_INSERT

class TabHostLayoutFragment : Fragment() {
    private val viewModel: TabHostLayoutViewModel by viewModels {
        TabHostLayoutViewModelFactory(this, arguments)
    }

    private val writeActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        childFragmentManager.fragments.forEach { fragment ->
            when (fragment) {
                is PostFragment -> fragment.onWriteActivityResult(result)
                is AlbumFragment -> fragment.onWriteActivityResult(result)
            }
        }
    }

    private var binding: FragmentTabHostLayoutBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabHostLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentList = arrayListOf(
            PostFragment.newInstance(viewModel.group.id, viewModel.group.groupName ?: "Unknown Group"),
            AlbumFragment.newInstance(viewModel.group.id),
            MemberFragment.newInstance(viewModel.group.id),
            SettingsFragment.newInstance(viewModel.group.id, viewModel.group.authorId)
        )
        binding.collapsingToolbar.isTitleEnabled = false

        (requireActivity() as? AppCompatActivity)?.run {
            title = viewModel.group.groupName

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
            Intent(context, CreatePostActivity::class.java).also { intent ->
                intent.putExtra("type", TYPE_INSERT)
                intent.putExtra("group_id", viewModel.group.id)
                writeActivityResultLauncher.launch(intent)
            }
        }
    }

    fun onMyInfoActivityResult(result: ActivityResult) {
        childFragmentManager.fragments.forEach { fragment ->
            when (fragment) {
                is PostFragment -> fragment.onMyInfoActivityResult(result)
                is AlbumFragment -> fragment.onMyInfoActivityResult(result)
                is MemberFragment -> fragment.onMyInfoActivityResult(result)
                is SettingsFragment -> fragment.onMyInfoActivityResult(result)
            }
        }
    }

    fun appbarLayoutExpand() {
        binding.appBarLayout.setExpanded(true)
    }

    companion object {
        fun newInstance(group: GroupItem.Group): Fragment = TabHostLayoutFragment().apply {
            arguments = Bundle().apply {
                putParcelable("group", group)
            }
        }
    }
}