package com.hhp227.application.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity

import com.hhp227.application.R
import com.hhp227.application.activity.MainActivity
import com.hhp227.application.databinding.FragmentChatListBinding
import com.hhp227.application.util.autoCleared

class ChatListFragment : Fragment() {
    private var binding: FragmentChatListBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activityMainBinding = (requireActivity() as MainActivity).binding

        (requireActivity() as AppCompatActivity).apply {
            title = getString(R.string.chat_fragment)

            setSupportActionBar(binding.toolbar)
            ActionBarDrawerToggle(this, activityMainBinding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close).let {
                activityMainBinding.drawerLayout.addDrawerListener(it)
                it.syncState()
            }
        }
    }

    companion object {
        fun newInstance(): Fragment = ChatListFragment()
    }
}
