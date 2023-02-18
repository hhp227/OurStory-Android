package com.hhp227.application.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.adapter.WriteListAdapter
import com.hhp227.application.databinding.FragmentUpdateReplyBinding
import com.hhp227.application.databinding.InputTextBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.UpdateReplyViewModel

class UpdateReplyFragment : Fragment(), MenuProvider {
    private var binding: FragmentUpdateReplyBinding by autoCleared()

    private val viewModel: UpdateReplyViewModel by viewModels {
        InjectorUtils.provideUpdateReplyViewModelFactory(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUpdateReplyBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = object : RecyclerView.Adapter<ItemHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder = ItemHolder(InputTextBinding.inflate(layoutInflater))

            override fun getItemCount(): Int = 1

            override fun onBindViewHolder(holder: ItemHolder, position: Int) { holder.bind() }
        }

        binding.toolbar.setupWithNavController(findNavController())
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        requireActivity().addMenuProvider(this)
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when {
                state.textError != null -> {
                    Snackbar.make(requireView(), getString(state.textError), Snackbar.LENGTH_LONG).setAction("Action", null).show()
                }
                state.isSuccess -> {
                    // 이상하게 필요없어지게 됨
                    /*val reply = viewModel.reply

                    setFragmentResult(findNavController().previousBackStackEntry?.destination?.displayName ?: "", bundleOf("reply" to reply))*/
                    findNavController().navigateUp()
                }
                state.error.isNotBlank() -> {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(this)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.write, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            android.R.id.home -> findNavController().navigateUp()
            R.id.action_send -> {
                viewModel.updateReply(viewModel.state.value?.text)
                true
            }
            else -> false
        }
    }

    inner class ItemHolder(val binding: InputTextBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.onValueChange = { viewModel.state.postValue(viewModel.state.value?.copy(text = it.toString())) }
            binding.text = viewModel.state.value?.text

            binding.executePendingBindings()
        }
    }
}