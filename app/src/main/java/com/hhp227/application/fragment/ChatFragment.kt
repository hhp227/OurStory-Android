package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.hhp227.application.R
import com.hhp227.application.adapter.ChatRoomAdapter
import com.hhp227.application.databinding.FragmentChatListBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.ChatViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// WIP
class ChatFragment : Fragment() {
    private val viewModel: ChatViewModel by viewModels {
        InjectorUtils.provideChatViewModelFactory()
    }

    private var binding: FragmentChatListBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireParentFragment().parentFragment as MainFragment).setNavAppbar(binding.toolbar)
        binding.recyclerView.apply {
            adapter = ChatRoomAdapter().apply {
                setOnItemClickListener { _, i ->
                    val directions = MainFragmentDirections.actionMainFragmentToChatMessageFragment(currentList[i].id, currentList[i].name)

                    requireActivity().findNavController(R.id.nav_host).navigate(directions)
                }
            }
        }
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                when {
                    state.isLoading -> showProgressBar()
                    state.chatRooms.isNotEmpty() -> {
                        hideProgressBar()
                        (binding.recyclerView.adapter as? ChatRoomAdapter)?.submitList(state.chatRooms)
                    }
                    state.error.isNotBlank() -> {
                        hideProgressBar()
                        Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .launchIn(lifecycleScope)
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
        fun newInstance(): Fragment = ChatFragment()
    }
}
