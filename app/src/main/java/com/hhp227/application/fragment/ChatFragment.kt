package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.hhp227.application.R
import com.hhp227.application.adapter.ChatRoomAdapter
import com.hhp227.application.databinding.FragmentChatListBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.ChatViewModel

class ChatFragment : Fragment() {
    private val viewModel: ChatViewModel by viewModels {
        InjectorUtils.provideChatViewModelFactory()
    }

    private var binding: FragmentChatListBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireParentFragment().parentFragment as MainFragment).setNavAppbar(binding.toolbar)
        binding.recyclerView.adapter = ChatRoomAdapter().apply {
            setOnItemClickListener { _, i ->
                val directions = MainFragmentDirections.actionMainFragmentToChatMessageFragment(currentList[i].id, currentList[i].name)

                requireActivity().findNavController(R.id.nav_host).navigate(directions)
            }
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when {
                state.chatRooms.isNotEmpty() -> {
                    viewModel.subscribeToTopic(state.chatRooms)
                }
                state.message.isNotBlank() -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        fun newInstance(): Fragment = ChatFragment()
    }
}