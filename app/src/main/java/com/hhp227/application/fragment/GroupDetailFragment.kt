package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hhp227.application.R
import com.hhp227.application.databinding.FragmentGroupDetailBinding
import com.hhp227.application.model.GroupItem
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.CreatePostViewModel.Companion.TYPE_INSERT

class GroupDetailFragment : Fragment(), MenuProvider {
    private val fragmentList by lazy {
        arrayListOf(
            PostFragment.newInstance(arguments?.getParcelable("group") ?: GroupItem.Group()),
            AlbumFragment.newInstance(arguments?.getParcelable("group") ?: GroupItem.Group()),
            MemberFragment.newInstance(arguments?.getParcelable("group") ?: GroupItem.Group()),
            SettingsFragment.newInstance(arguments?.getParcelable("group") ?: GroupItem.Group())
        )
    }

    private var binding: FragmentGroupDetailBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGroupDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.group = arguments?.getParcelable("group")
        binding.isTabPositionZero = binding.viewPager.currentItem == 0
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNavAppBar(binding.toolbar)
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
                binding.isTabPositionZero = tab?.position == 0

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
            val directions = GroupDetailFragmentDirections.actionGroupDetailFragmentToCreatePostFragment(TYPE_INSERT, binding.group!!.id)

            findNavController().navigate(directions)
        }
        setFragmentResultListener(findNavController().currentDestination?.displayName ?: "") { _, b ->
            childFragmentManager.fragments.forEach { fragment ->
                when (fragment) {
                    is PostFragment -> fragment.onFragmentResult(b)
                    is AlbumFragment -> fragment.onFragmentResult(b)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(this)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.group, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_chat -> {
                val directions = GroupDetailFragmentDirections.actionGroupDetailFragmentToChatMessageFragment(binding.group!!.id)

                findNavController().navigate(directions)
                true
            }
            else -> false
        }
    }

    private fun setNavAppBar(toolbar: Toolbar) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).addMenuProvider(this)
        binding.collapsingToolbar.setupWithNavController(toolbar, findNavController())
    }

    fun setAppbarLayoutExpand(isExpanded: Boolean) {
        binding.appBarLayout.setExpanded(isExpanded)
    }
}