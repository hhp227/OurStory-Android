package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.databinding.FragmentUpdateReplyBinding
import com.hhp227.application.databinding.InputTextBinding
import com.hhp227.application.dto.ListItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.UpdateReplyViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class UpdateReplyFragment : Fragment() {
    private var binding: FragmentUpdateReplyBinding by autoCleared()

    private val viewModel: UpdateReplyViewModel by viewModels {
        InjectorUtils.provideUpdateReplyViewModelFactory(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUpdateReplyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = object : RecyclerView.Adapter<ItemHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder = ItemHolder(InputTextBinding.inflate(layoutInflater))

            override fun getItemCount(): Int = 1

            override fun onBindViewHolder(holder: ItemHolder, position: Int) {
                holder.bind(viewModel.reply)
            }
        }

        binding.toolbar.apply {
            setupWithNavController(findNavController())
            inflateMenu(R.menu.write)
            setOnMenuItemClickListener(::onOptionsItemSelected)
        }
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                when {
                    state.isLoading -> {

                    }
                    state.text != null -> {
                        val reply = viewModel.reply.apply {
                            reply = state.text
                        }

                        setFragmentResult(findNavController().previousBackStackEntry?.destination?.displayName ?: "", bundleOf("reply" to reply))
                        findNavController().navigateUp()
                    }
                    state.error.isNotBlank() -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .launchIn(lifecycleScope)
        viewModel.textFieldState
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                state.textError?.let { error -> Snackbar.make(requireView(), getString(error), Snackbar.LENGTH_LONG).setAction("Action", null).show() }
            }
            .launchIn(lifecycleScope)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_send -> {
            val text = (binding.recyclerView.getChildViewHolder(binding.recyclerView.getChildAt(0)) as ItemHolder).binding.etText.text.toString()

            viewModel.updateReply(text)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    inner class ItemHolder(val binding: InputTextBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(replyItem: ListItem.Reply) {
            binding.etText.setText(replyItem.reply)
        }
    }
}