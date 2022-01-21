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
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.hhp227.application.*
import com.hhp227.application.activity.WriteActivity
import com.hhp227.application.activity.WriteActivity.Companion.TYPE_INSERT
import com.hhp227.application.app.AppController
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
            viewModel.groupName = it.getString("group_name")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabHostLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentList = arrayListOf(Tab1Fragment.newInstance(viewModel.groupId, viewModel.groupName), Tab2Fragment.newInstance(), Tab3Fragment.newInstance(viewModel.groupId), Tab4Fragment.newInstance(viewModel.groupId, viewModel.authorId))

        (requireActivity() as? AppCompatActivity)?.run {
            title = viewModel.groupName

            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding.tabLayout.apply {
            setupWithViewPager(binding.viewPager)
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabSelected(tab: TabLayout.Tab) {
                    binding.viewPager.currentItem = tab.position
                    binding.fab.visibility = if (tab.position != 0) View.GONE else View.VISIBLE
                }
            })
        }
        binding.viewPager.apply {
            offscreenPageLimit = fragmentList.size
            adapter = object : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
                override fun getItem(position: Int): Fragment = fragmentList[position]

                override fun getCount(): Int = fragmentList.size

                override fun getPageTitle(position: Int): CharSequence? = TAB_NAMES[position]
            }

            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout))
        }
        binding.fab.setOnClickListener {
            Intent(context, WriteActivity::class.java).also { intent ->
                intent.putExtra("type", TYPE_INSERT)
                intent.putExtra("text", "")
                intent.putExtra("group_id", viewModel.groupId)
                startActivityForResult(intent, UPDATE_CODE)
            }
        }
        binding.collapsingToolbar.isTitleEnabled = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        childFragmentManager.fragments.forEach { fragment -> fragment.onActivityResult(requestCode, resultCode, data) }
        if ((requestCode == UPDATE_CODE || requestCode == POST_INFO_CODE) && resultCode == RESULT_OK)
            binding.appBarLayout.setExpanded(true)
    }

    companion object {
        private val TAB_NAMES = AppController.getInstance().resources.getStringArray(R.array.tab_name)

        fun newInstance(groupId: Int, authorId: Int, groupName: String?): Fragment = TabHostLayoutFragment().apply {
            arguments = Bundle().apply {
                putInt("group_id", groupId)
                putInt("author_id", authorId)
                putString("group_name", groupName)
            }
        }
    }
}