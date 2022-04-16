package com.hhp227.application.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hhp227.application.R
import com.hhp227.application.activity.ChatMessageActivity
import com.hhp227.application.databinding.FragmentGroupDetailBinding
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.CreatePostViewModel.Companion.TYPE_INSERT
import com.hhp227.application.viewmodel.GroupDetailViewModel
import com.hhp227.application.viewmodel.GroupDetailViewModelFactory

class GroupDetailFragment : Fragment() {
    private val viewModel: GroupDetailViewModel by viewModels {
        GroupDetailViewModelFactory(this, arguments)
    }

    private val fragmentList by lazy {
        arrayListOf(
            PostFragment.newInstance(viewModel.group.id, viewModel.group.groupName ?: "Unknown Group"),
            AlbumFragment.newInstance(viewModel.group.id),
            MemberFragment.newInstance(viewModel.group.id),
            SettingsFragment.newInstance(viewModel.group.id, viewModel.group.authorId)
        )
    }

    private var binding: FragmentGroupDetailBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGroupDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.apply {
            title = viewModel.group.groupName

            inflateMenu(R.menu.group)
            setOnMenuItemClickListener(::onOptionsItemSelected)
        }
        binding.collapsingToolbar.setupWithNavController(binding.toolbar, findNavController())
        binding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@GroupDetailFragment) {
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

                when (val fragment = fragmentList[tab?.position ?: 0]) {
                    is PostFragment -> {
                        if (!fragment.isFirstItemVisible()) {
                            setAppbarLayoutExpand(false)
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })
        binding.fab.setOnClickListener {
            val directions = GroupDetailFragmentDirections.actionGroupDetailFragmentToCreatePostFragment(TYPE_INSERT, viewModel.group.id)

            findNavController().navigate(directions)
        }
        setFragmentResultListener(findNavController().currentDestination?.displayName ?: "") { k, b ->
            childFragmentManager.fragments.forEach { fragment ->
                when (fragment) {
                    is PostFragment -> fragment.onFragmentResult(b)
                    is AlbumFragment -> fragment.onFragmentResult(b)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_chat -> {
                startActivity(Intent(requireContext(), ChatMessageActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun setAppbarLayoutExpand(isExpanded: Boolean) {
        binding.appBarLayout.setExpanded(isExpanded)
    }
}