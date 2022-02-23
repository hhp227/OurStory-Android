package com.hhp227.application.fragment

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hhp227.application.adapter.PostGridAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.data.PostRepository
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.dto.ListItem
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.AlbumViewModel
import com.hhp227.application.viewmodel.AlbumViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AlbumFragment : Fragment() {
    private val viewModel: AlbumViewModel by viewModels {
        AlbumViewModelFactory(PostRepository(), this, arguments)
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

    fun onWriteActivityResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            viewModel.refreshPostList()
        }
    }

    fun onPostDetailActivityResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            result.data
                ?.also { intent -> viewModel.updatePost(intent.getParcelableExtra("post") ?: ListItem.Post()) }
                ?: viewModel.refreshPostList()
        }
    }

    fun onMyInfoActivityResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            (binding.recyclerView.adapter as PostGridAdapter).also { adapter ->
                adapter.currentList
                    .mapIndexed { index, post -> index to post }
                    .filter { (_, a) -> a is ListItem.Post && a.userId == AppController.getInstance().preferenceManager.user.id }
                    .forEach { (i, _) ->
                        if (adapter.currentList.isNotEmpty()) {
                            (adapter.currentList[i] as ListItem.Post).profileImage = AppController.getInstance().preferenceManager.user.profileImage

                            adapter.notifyItemChanged(i)
                        }
                    }
            }
        }
    }

    companion object {
        private const val ARG_PARAM1 = "group_id"

        fun newInstance(groupId: Int) =
            AlbumFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, groupId)
                }
            }
    }
}