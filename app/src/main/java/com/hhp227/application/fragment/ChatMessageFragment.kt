package com.hhp227.application.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.adapter.MessageListAdapter
import com.hhp227.application.app.Config
import com.hhp227.application.databinding.FragmentChatMessageBinding
import com.hhp227.application.dto.MessageItem
import com.hhp227.application.fcm.NotificationUtils
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.ChatMessageViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChatMessageFragment : Fragment() {
    private var binding: FragmentChatMessageBinding by autoCleared()

    private val viewModel: ChatMessageViewModel by viewModels {
        InjectorUtils.provideChatMessageViewModelFactory(this)
    }

    private val registrationBroadcastReceiver: BroadcastReceiver by lazy(::RegistrationBroadcastReceiver)

    private val onLayoutChangeListener = View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
        if (bottom > oldBottom) {
            (binding.rvMessages.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(binding.rvMessages.childCount, 10)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setupWithNavController(findNavController())
        binding.rvMessages.apply {
            adapter = MessageListAdapter()

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    /*if (!viewModel.state.value.hasRequestedMore && !recyclerView.canScrollVertically(-1)) {
                        if (recyclerView.adapter?.itemCount == viewModel.state.value.previousMessageCnt) return
                        viewModel.state.value.offset = recyclerView.adapter?.itemCount ?: 0
                        viewModel.state.value.hasRequestedMore = true

                        fetchChatThread()
                    }*/
                    if (!recyclerView.canScrollVertically(-1)) {
                        viewModel.fetchNextPage()
                    }
                }
            })
            addOnLayoutChangeListener(onLayoutChangeListener)
        }
        binding.etInputMsg.doOnTextChanged { text, _, _, _ ->
            binding.tvSend.setBackgroundResource(if (!TextUtils.isEmpty(text)) R.drawable.background_sendbtn_p else R.drawable.background_sendbtn_n)
            binding.tvSend.setTextColor(ContextCompat.getColor(requireContext(), if (!TextUtils.isEmpty(text)) android.R.color.white else android.R.color.darker_gray))
        }
        binding.tvSend.setOnClickListener { viewModel.sendMessage(binding.etInputMsg.text.trim().toString()) }
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { state ->
                when {
                    state.listMessages.isNotEmpty() -> {
                        (binding.rvMessages.adapter as MessageListAdapter).submitList(state.listMessages)
                        /*Handler(Looper.getMainLooper()).postDelayed({
                            (binding.rvMessages.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(binding.rvMessages.childCount, 10)
                        }, 100)*/
                    }
                    state.messageId >= 0 -> {
                        binding.etInputMsg.setText("")
                    }
                }
            }
            .launchIn(lifecycleScope)
        viewModel.userFlow
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { user ->
                (binding.rvMessages.adapter as MessageListAdapter).userId = user?.id ?: 0
            }
            .launchIn(lifecycleScope)
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
        val message = intent.getSerializableExtra("message") as MessageItem?
        val chatRoomId = intent.getStringExtra("chat_room_id")

        if (message != null && chatRoomId != null) {
            viewModel.addItem(message)
            binding.rvMessages.scrollToPosition(binding.rvMessages.childCount)
        }
    }

    private fun onLoadMoreItems(addCount: Int) {
        viewModel.state.value.previousMessageCnt = viewModel.state.value.listMessages.size - addCount
        viewModel.state.value.hasRequestedMore = false

        (binding.rvMessages.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(addCount, 10)
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