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
import com.hhp227.application.adapter.PostListAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.dto.ListItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.MyPostViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MyPostFragment : Fragment() {
    private val viewModel: MyPostViewModel by viewModels {
        InjectorUtils.provideMyPostViewModelFactory()
    }

    private var binding: FragmentTabBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val entry = AppController.getInstance().requestQueue.cache[URLs.URL_USER_POSTS]

        binding.recyclerView.apply {
            adapter = PostListAdapter().apply {
                setLoaderVisibility(View.INVISIBLE)
                setOnItemClickListener(object : PostListAdapter.OnItemClickListener {
                    override fun onItemClick(v: View, p: Int) {
                        // TODO
                    }

                    override fun onLikeClick(p: Int) {
                        // TODO
                    }
                })
            }

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    /*if (!hasRequestedMore && dy > 0 && layoutManager != null && (layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() >= layoutManager!!.itemCount - 1) {
                        hasRequestedMore = true
                        offset = viewModel.postItems.size - 1

                        setLoaderVisibility(View.VISIBLE)
                        notifyItemChanged(viewModel.postItems.size - 1)
                        fetchPostList()
                    }*/
                }
            })
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                delay(1000)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.postItems.isNotEmpty() -> {
                    hideProgressBar()
                    (binding.recyclerView.adapter as PostListAdapter).submitList(state.postItems)
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

    fun profileUpdateResult() {
        (binding.recyclerView.adapter as PostListAdapter).also { adapter ->
            adapter.currentList
            .map { if (it is ListItem.Post) it.profileImage = AppController.getInstance().preferenceManager.user?.profileImage else it }
            .indices
            .forEach { adapter.notifyItemChanged(it) }
        }
    }

    companion object {
        fun newInstance(): Fragment = MyPostFragment()
    }
}