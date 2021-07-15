package com.hhp227.application.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.messaging.FirebaseMessaging

import com.hhp227.application.R
import com.hhp227.application.activity.ChatActivity
import com.hhp227.application.activity.MainActivity
import com.hhp227.application.adapter.ChatRoomAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentChatListBinding
import com.hhp227.application.dto.ChatRoomItem
import com.hhp227.application.util.autoCleared

class ChatListFragment : Fragment() {
    private val chatRooms by lazy { mutableListOf<ChatRoomItem>() }

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
        binding.recyclerView.apply {
            adapter = ChatRoomAdapter().apply {
                submitList(chatRooms)
                setOnItemClickListener { v, i ->
                    val intent = Intent(requireContext(), ChatActivity::class.java)
                        .putExtra("chat_room_id", chatRooms[i].id)
                        .putExtra("name", chatRooms[i].name)

                    startActivity(intent)
                }
            }
        }
        fetchDataTask()
    }

    private fun fetchDataTask() {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, URLs.URL_CHAT_ROOMS, null, { response ->
                if (!response.getBoolean("error")) {
                    val chatRoomArray = response.getJSONArray("chat_rooms")

                    for (i in 0 until chatRoomArray.length()) {
                        with(chatRoomArray.getJSONObject(i)) {
                            val chatRoom = ChatRoomItem(
                                id = getInt("chat_room_id"),
                                name = getString("name"),
                                timeStamp = getString("created_at")
                            )

                            chatRooms.add(chatRoom)
                            binding.recyclerView.adapter?.notifyItemChanged(chatRooms.size - 1)
                            FirebaseMessaging.getInstance().subscribeToTopic("topic_${chatRoom.id}")
                        }
                    }
                }
            }, { error ->
                VolleyLog.e(TAG, error.message)
            })

        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
    }

    companion object {
        private val TAG = ChatListFragment::class.simpleName

        fun newInstance(): Fragment = ChatListFragment()
    }
}
