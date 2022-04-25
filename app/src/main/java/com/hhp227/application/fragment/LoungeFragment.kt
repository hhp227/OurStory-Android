package com.hhp227.application.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.adapter.PostListAdapter
import com.hhp227.application.databinding.FragmentLoungeBinding
import com.hhp227.application.dto.ListItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.CreatePostViewModel.Companion.TYPE_INSERT
import com.hhp227.application.viewmodel.LoungeViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoungeFragment : Fragment() {
    private val viewModel: LoungeViewModel by viewModels {
        InjectorUtils.provideLoungeViewModelFactory()
    }

    private lateinit var binding: FragmentLoungeBinding

    private var scrollListener: RecyclerView.OnScrollListener by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoungeBinding.inflate(inflater, container, false)
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(RecyclerView.LAYOUT_DIRECTION_RTL)) {
                    viewModel.fetchNextPage()
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireParentFragment().parentFragment as? MainFragment)?.setNavAppbar(binding.toolbar)
        binding.recyclerView.apply {
            adapter = PostListAdapter().apply {
                setOnItemClickListener(object : PostListAdapter.OnItemClickListener {
                    override fun onItemClick(v: View, p: Int) {
                        (currentList[p] as? ListItem.Post)?.also { post ->
                            val directions = MainFragmentDirections.actionMainFragmentToPostDetailFragment(post, v.id == R.id.ll_reply, null)

                            requireActivity().findNavController(R.id.nav_host).navigate(directions)
                        }
                    }

                    override fun onLikeClick(p: Int) {
                        (currentList[p] as? ListItem.Post)?.also { post ->
                            viewModel.togglePostLike(post)
                        }
                    }
                })
            }

            addOnScrollListener(scrollListener)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipeRefreshLayout.isRefreshing = false

                viewModel.refreshPostList()
            }, 1000)
        }
        binding.fab.setOnClickListener {
            val directions = MainFragmentDirections.actionMainFragmentToCreatePostFragment(TYPE_INSERT, 0)

            requireActivity().findNavController(R.id.nav_host).navigate(directions)
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.hasRequestedMore -> viewModel.fetchPostList(offset = state.offset)
                state.offset == 0 -> Handler(Looper.getMainLooper()).postDelayed({
                    binding.appBarLayout.setExpanded(true, false)
                    binding.recyclerView.scrollToPosition(0)
                }, 500)
                state.itemList.isNotEmpty() -> {
                    hideProgressBar()
                    (binding.recyclerView.adapter as PostListAdapter).submitList(state.itemList)
                }
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
        viewModel.userFlow.onEach { user ->
            if (user != null) {
                (binding.recyclerView.adapter as PostListAdapter).also { adapter ->
                    adapter.currentList
                        .mapIndexed { index, post -> index to post }
                        .filter { (_, a) -> a is ListItem.Post && a.userId == user.id }
                        .forEach { (i, _) ->
                            if (adapter.currentList.isNotEmpty()) {
                                (adapter.currentList[i] as ListItem.Post).profileImage = user.profileImage

                                adapter.notifyItemChanged(i)
                            }
                        }
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    fun onFragmentResult(bundle: Bundle) {
        bundle.getParcelable<ListItem.Post>("post")
            ?.also(viewModel::updatePost)
            ?: viewModel.refreshPostList()
    }
}