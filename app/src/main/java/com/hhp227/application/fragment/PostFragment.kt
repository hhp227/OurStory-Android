package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.hhp227.application.R
import com.hhp227.application.adapter.ItemLoadStateAdapter
import com.hhp227.application.adapter.PostPagingDataAdapter
import com.hhp227.application.databinding.FragmentPostBinding
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.ListItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.PostViewModel

// WIP
class PostFragment : Fragment() {
    private val viewModel: PostViewModel by viewModels {
        InjectorUtils.providePostViewModelFactory(this)
    }

    private var binding: FragmentPostBinding by autoCleared()

    private val adapter = PostPagingDataAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPostBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.recyclerView.adapter = adapter.withLoadStateFooter(ItemLoadStateAdapter(adapter::retry))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.setOnItemClickListener(object : PostPagingDataAdapter.OnItemClickListener {
            override fun onItemClick(v: View, p: Int) {
                val post = adapter.snapshot().items[p]
                val directions = GroupDetailFragmentDirections.actionGroupDetailFragmentToPostDetailFragment(post, v.id == R.id.ll_reply, viewModel.group.groupName)

                binding.recyclerView.findNavController().navigate(directions)
            }

            override fun onLikeClick(p: Int) {
                val post = adapter.snapshot().items[p]

                viewModel.togglePostLike(post)
            }
        })
        binding.swipeRefreshLayout.setOnRefreshListener(adapter::refresh)
        adapter.loadState.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it.mediator?.refresh is LoadState.Loading
            binding.isLoading = it.refresh is LoadState.Loading
        }
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                adapter.updateProfileImages(user)
            }
        }
        /*if (viewModel.postItems.size < 2) {
            viewModel.postItems.add(0, PostItem.Empty(R.drawable.ic_baseline_library_add_72, getString(R.string.add_message)))
        }*/
    }

    fun onFragmentResult(bundle: Bundle) {
        Toast.makeText(requireContext(), "onFragmentResult $bundle", Toast.LENGTH_LONG).show()
        /*bundle.getParcelable<ListItem.Post>("post")
            ?.also(viewModel::updatePost)
            ?: adapter.refresh()*/
    }

    fun isFirstItemVisible() = (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 0

    companion object {
        private const val ARG_PARAM = "group"

        fun newInstance(group: GroupItem.Group) =
            PostFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM, group)
                }
            }
    }
}