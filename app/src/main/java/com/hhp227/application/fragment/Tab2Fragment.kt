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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hhp227.application.adapter.PostGridAdapter
import com.hhp227.application.data.PostRepository
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.Tab2ViewModel
import com.hhp227.application.viewmodel.Tab2ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class Tab2Fragment : Fragment() {
    private val viewModel: Tab2ViewModel by viewModels {
        Tab2ViewModelFactory(PostRepository(), this, arguments)
    }

    private var binding: FragmentTabBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                delay(1000)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
                //gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
            }
            adapter = PostGridAdapter()
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.postItems.isNotEmpty() -> {
                    hideProgressBar()
                    (binding.recyclerView.adapter as PostGridAdapter).submitList(state.postItems)
                }
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    companion object {
        private const val ARG_PARAM1 = "group_id"

        fun newInstance(groupId: Int) =
            Tab2Fragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, groupId)
                }
            }
    }
}