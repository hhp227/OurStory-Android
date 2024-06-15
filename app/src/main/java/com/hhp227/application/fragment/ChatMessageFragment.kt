package com.hhp227.application.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.hhp227.application.adapter.MessagePagingAdapter
import com.hhp227.application.app.Config
import com.hhp227.application.databinding.FragmentChatMessageBinding
import com.hhp227.application.fcm.NotificationUtils
import com.hhp227.application.model.ChatItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.ChatMessageViewModel

class ChatMessageFragment : Fragment() {
    private var binding: FragmentChatMessageBinding by autoCleared()

    private val viewModel: ChatMessageViewModel by viewModels {
        InjectorUtils.provideChatMessageViewModelFactory(this)
    }

    private val registrationBroadcastReceiver: BroadcastReceiver by lazy(::RegistrationBroadcastReceiver)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatMessageBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.rvMessages.adapter = MessagePagingAdapter(viewModel.state.value?.user?.id ?: -1)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setupWithNavController(findNavController())
        binding.rvMessages.adapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart > 0) {
                    binding.rvMessages.scrollToPosition(positionStart - 1)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        // registering the receiver for new notification
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(registrationBroadcastReceiver, IntentFilter(Config.PUSH_NOTIFICATION))
        NotificationUtils.clearNotifications()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(registrationBroadcastReceiver)
        super.onPause()
    }

    private fun handlePushNotification(intent: Intent) {
        val message = intent.getSerializableExtra("message") as ChatItem.Message?
        val chatRoomId = intent.getStringExtra("chat_room_id")

        if (message != null && chatRoomId != null) {
            viewModel.addChatMessage()
        }
    }

    inner class RegistrationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Config.PUSH_NOTIFICATION) {
                // new push message is received
                handlePushNotification(intent)
            }
        }
    }
}