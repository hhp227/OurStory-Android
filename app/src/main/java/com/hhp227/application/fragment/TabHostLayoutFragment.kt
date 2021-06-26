package com.hhp227.application.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.recreate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.hhp227.application.*
import com.hhp227.application.Tab1Fragment.FEEDINFO_CODE
import com.hhp227.application.activity.WriteActivity
import com.hhp227.application.activity.WriteActivity.Companion.TYPE_INSERT
import com.hhp227.application.app.AppController
import com.hhp227.application.fragment.GroupFragment.Companion.UPDATE_CODE
import kotlinx.android.synthetic.main.fragment_tab_host_layout.*
import kotlin.properties.Delegates

class TabHostLayoutFragment : Fragment() {
    private var mGroupId by Delegates.notNull<Int>()

    private var mAuthorId by Delegates.notNull<Int>()

    private lateinit var mGroupName: String

    private lateinit var mActivity: AppCompatActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mGroupId = it.getInt("group_id")
            mAuthorId = it.getInt("author_id")
            mGroupName = it.getString("group_name")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_host_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentList = arrayListOf<Fragment>(Tab1Fragment.newInstance(mGroupId, mGroupName), Tab2Fragment.newInstance(), Tab3Fragment.newInstance(mGroupId), Tab4Fragment.newInstance(mGroupId, mAuthorId))
        mActivity = activity as AppCompatActivity

        mActivity.run {
            title = mGroupName

            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        tabLayout.apply {
            setupWithViewPager(viewPager)
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    viewPager.currentItem = tab!!.position
                    fab.visibility = if (tab.position != 0) View.GONE else View.VISIBLE
                }
            })
        }
        viewPager.apply {
            offscreenPageLimit = fragmentList.size
            adapter = object : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
                override fun getItem(position: Int): Fragment = fragmentList[position]

                override fun getCount(): Int = fragmentList.size

                override fun getPageTitle(position: Int): CharSequence? = TAB_NAMES[position]
            }

            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        }
        fab.setOnClickListener {
            Intent(context, WriteActivity::class.java).also { intent ->
                intent.putExtra("type", TYPE_INSERT)
                intent.putExtra("text", "")
                intent.putExtra("group_id", mGroupId)
                startActivityForResult(intent, UPDATE_CODE)
            }
        }
        collapsingToolbar.isTitleEnabled = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        childFragmentManager.fragments.forEach { fragment -> fragment.onActivityResult(requestCode, resultCode, data) }
        if ((requestCode == UPDATE_CODE || requestCode == FEEDINFO_CODE) && resultCode == RESULT_OK)
            appbarLayout.setExpanded(true)
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