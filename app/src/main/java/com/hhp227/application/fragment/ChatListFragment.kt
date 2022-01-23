package com.hhp227.application.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels

import com.hhp227.application.R
import com.hhp227.application.activity.ChatActivity
import com.hhp227.application.activity.MainActivity
import com.hhp227.application.adapter.ChatRoomAdapter
import com.hhp227.application.databinding.FragmentChatListBinding
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.ChatListViewModel

class ChatListFragment : Fragment() {
    private val viewModel: ChatListViewModel by viewModels()

    private var binding: FragmentChatListBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        viewModel.state.observe(this) { state ->
            when {
                state.isLoading -> showProgressBar()
                state.chatRooms.isNotEmpty() -> {
                    hideProgressBar()
                    binding.recyclerView.apply {
                        adapter = ChatRoomAdapter().apply {
                            submitList(state.chatRooms)
                            setOnItemClickListener { v, i ->
                                val intent = Intent(requireContext(), ChatActivity::class.java)
                                    .putExtra("chat_room_id", state.chatRooms[i].id)
                                    .putExtra("name", state.chatRooms[i].name)

                                startActivity(intent)
                            }
                        }
                    }
                }
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showProgressBar() {
        if (binding.progressBar.visibility == View.GONE)
            binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        if (binding.progressBar.visibility == View.VISIBLE)
            binding.progressBar.visibility = View.GONE
    }

    companion object {
        private val TAG = ChatListFragment::class.simpleName

        fun newInstance(): Fragment = ChatListFragment()
    }
}
