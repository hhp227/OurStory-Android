package com.hhp227.application.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.adapter.GroupListAdapter
import com.hhp227.application.databinding.FragmentGroupFindBinding
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.viewmodel.JoinRequestGroupViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class JoinRequestGroupFragment : Fragment() {
    private lateinit var binding: FragmentGroupFindBinding

    private val viewModel: JoinRequestGroupViewModel by viewModels {
        InjectorUtils.provideJoinRequestGroupViewModelFactory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGroupFindBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setupWithNavController(findNavController())
        binding.recyclerView.apply {
            adapter = GroupListAdapter().apply {
                setOnItemClickListener { _, position ->
                    if (position != RecyclerView.NO_POSITION) {
                        val groupItem = currentList[position] as GroupItem.Group
                        val directions = JoinRequestGroupFragmentDirections.actionJoinRequestGroupFragmentToGroupInfoFragment(groupItem)

                        findNavController().navigate(directions)
                    }
                }
            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipeRefreshLayout.isRefreshing = false

                viewModel.refreshGroupList()
            }, 1000)
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.groupList.isNotEmpty() -> {
                    hideProgressBar()
                    (binding.recyclerView.adapter as GroupListAdapter).submitList(state.groupList)
                }
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
        setFragmentResultListener("${findNavController().currentBackStackEntry?.destination?.id}") { k, b ->
            viewModel.refreshGroupList()
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
}
