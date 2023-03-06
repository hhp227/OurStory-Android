package com.hhp227.application.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.hhp227.application.databinding.FragmentGroupJoinRequestBinding
import com.hhp227.application.model.GroupItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.JoinRequestGroupViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// WIP
class JoinRequestGroupFragment : Fragment() {
    private var binding: FragmentGroupJoinRequestBinding by autoCleared()

    private val viewModel: JoinRequestGroupViewModel by viewModels {
        InjectorUtils.provideJoinRequestGroupViewModelFactory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGroupJoinRequestBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setupWithNavController(findNavController())
        /*binding.recyclerView.apply {
            adapter = GroupListAdapter().apply {
                setOnItemClickListener { _, position ->
                    if (position != RecyclerView.NO_POSITION) {
                        val groupItem = currentList[position] as GroupItem.Group
                        val directions = JoinRequestGroupFragmentDirections.actionJoinRequestGroupFragmentToGroupInfoFragment(groupItem)

                        findNavController().navigate(directions)
                    }
                }
            }

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(RecyclerView.LAYOUT_DIRECTION_RTL)) {
                        viewModel.fetchNextPage()
                    }
                }
            })
        }*/
        /*binding.swipeRefreshLayout.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipeRefreshLayout.isRefreshing = false

                viewModel.refreshGroupList()
            }, 1000)
        }
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                when {
                    state.isLoading -> showProgressBar()
                    state.hasRequestedMore -> viewModel.fetchGroupList(state.offset)
                    state.groupList.isNotEmpty() -> {
                        hideProgressBar()
                        (binding.recyclerView.adapter as GroupListAdapter).submitList(state.groupList)
                    }
                    state.error.isNotBlank() -> {
                        hideProgressBar()
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .launchIn(lifecycleScope)*/
        setFragmentResultListener("${findNavController().currentBackStackEntry?.destination?.id}") { _, b ->
            //viewModel.refreshGroupList()
        }
        viewModel.groups.observe(viewLifecycleOwner) {
            Log.e("TEST", "Test: $it")
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
