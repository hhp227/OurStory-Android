package com.hhp227.application.fragment

import PostFragment
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hhp227.application.*
import com.hhp227.application.activity.WriteActivity
import com.hhp227.application.databinding.FragmentTabHostLayoutBinding
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.TabHostLayoutViewModel
import com.hhp227.application.viewmodel.WriteViewModel.Companion.TYPE_INSERT

class TabHostLayoutFragment : Fragment() {
    private val viewModel: TabHostLayoutViewModel by viewModels()

    private val writeActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        childFragmentManager.fragments.forEach { fragment ->
            when (fragment) {
                is PostFragment -> fragment.onWriteActivityResult(result)
                is AlbumFragment -> fragment.onWriteActivityResult(result)
            }
        }
    }

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
        val fragmentList = arrayListOf(PostFragment.newInstance(viewModel.groupId, viewModel.groupName), AlbumFragment.newInstance(viewModel.groupId), MemberFragment.newInstance(viewModel.groupId), SettingsFragment.newInstance(viewModel.groupId, viewModel.authorId))
        binding.collapsingToolbar.isTitleEnabled = false

        (requireActivity() as? AppCompatActivity)?.run {
            title = arguments?.getString("group_name")

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
                writeActivityResultLauncher.launch(intent)
            }
        }
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 프로필 이미지 업데이트 때문에 남김
        //TODO 버그있어서 하위프래그먼트들의 업데이트들을 어쩔수가 없다.
        Log.e("TEST", "TabHostLayoutFragment onActivityResult $requestCode, $resultCode, $data")
        childFragmentManager.fragments.forEach { fragment -> fragment.onActivityResult(requestCode, resultCode, data) }
    }*/

    fun appbarLayoutExpand() {
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