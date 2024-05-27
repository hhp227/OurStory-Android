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
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hhp227.application.R
import com.hhp227.application.adapter.PostGridAdapter
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.ListItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.AlbumViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// WIP
class AlbumFragment : Fragment() {
    private val viewModel: AlbumViewModel by viewModels {
        InjectorUtils.provideAlbumViewModelFactory(this)
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

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // TODO
                }
            })
        }
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { state ->
                when {
                    state.isLoading -> showProgressBar()
                    state.postItems.isNotEmpty() -> {
                        hideProgressBar()
                        (binding.recyclerView.adapter as PostGridAdapter).submitList(state.postItems)
                    }
                    state.message.isNotBlank() -> {
                        hideProgressBar()
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        (binding.recyclerView.adapter as PostGridAdapter).submitList(listOf(ListItem.Empty(R.drawable.ic_baseline_library_add_72, getString(R.string.add_message))))
                    }
                }
            }
            .launchIn(lifecycleScope)
        viewModel.userFlow
            .onEach { user ->
                (binding.recyclerView.adapter as PostGridAdapter).also { adapter ->
                    adapter.currentList
                        .mapIndexed { index, post -> index to post }
                        .filter { (_, a) -> a is ListItem.Post && a.userId == user?.id }
                        .forEach { (i, _) ->
                            if (adapter.currentList.isNotEmpty()) {
                                (adapter.currentList[i] as ListItem.Post).profileImage = user?.profileImage

                                adapter.notifyItemChanged(i)
                            }
                        }
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.run { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.run { visibility = View.GONE }

    fun onFragmentResult(bundle: Bundle) {
        bundle.getParcelable<ListItem.Post>("post")
            ?.also(viewModel::updatePost)
            ?: viewModel.refreshPostList()
    }

    companion object {
        private const val ARG_PARAM = "group"

        fun newInstance(group: GroupItem.Group) =
            AlbumFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM, group)
                }
            }
    }
}