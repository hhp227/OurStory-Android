package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.adapter.ItemLoadStateAdapter
import com.hhp227.application.adapter.PostPagingDataAdapter
import com.hhp227.application.databinding.FragmentLoungeBinding
import com.hhp227.application.model.ListItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.CreatePostViewModel.Companion.TYPE_INSERT
import com.hhp227.application.viewmodel.LoungeViewModel

class LoungeFragment : Fragment() {
    private val viewModel: LoungeViewModel by viewModels {
        InjectorUtils.provideLoungeViewModelFactory()
    }

    private val adapter = PostPagingDataAdapter()

    private var binding: FragmentLoungeBinding by autoCleared()

    private lateinit var adapterDataObserver: RecyclerView.AdapterDataObserver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoungeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.recyclerView.adapter = adapter.withLoadStateFooter(ItemLoadStateAdapter(adapter::retry))
        adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0 && (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() >= 0) {
                    binding.appBarLayout.setExpanded(true)
                    binding.recyclerView.scrollToPosition(positionStart)
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireParentFragment().parentFragment as? MainFragment)?.setNavAppbar(binding.toolbar)
        binding.swipeRefreshLayout.setOnRefreshListener(::refresh)
        adapter.setOnItemClickListener(object : PostPagingDataAdapter.OnItemClickListener {
            override fun onItemClick(v: View, p: Int) {
                val post = adapter.snapshot().items[p]
                val directions = MainFragmentDirections.actionMainFragmentToPostDetailFragment(post, v.id == R.id.ll_reply, null)

                requireActivity().findNavController(R.id.nav_host).navigate(directions)
            }

            override fun onLikeClick(p: Int) {
                val post = adapter.snapshot().items[p]

                viewModel.togglePostLike(post)
            }
        })
        adapter.registerAdapterDataObserver(adapterDataObserver)
        binding.fab.setOnClickListener {
            val directions = MainFragmentDirections.actionMainFragmentToCreatePostFragment(TYPE_INSERT, 0)

            requireActivity().findNavController(R.id.nav_host).navigate(directions)
        }
        subscribeUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.unregisterAdapterDataObserver(adapterDataObserver)
    }

    private fun subscribeUi() {
        adapter.loadState.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it.mediator?.refresh is LoadState.Loading
            binding.isLoading = it.refresh is LoadState.Loading
        }
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                adapter.updateProfileImages(user)
            }
        }
    }

    private fun refresh() {
        viewModel.refresh()
        adapter.refresh()
    }

    fun onFragmentResult(bundle: Bundle) {
        bundle.getParcelable<ListItem.Post>("post")
            ?.also(viewModel::onDeletePost)
            ?: refresh()
    }
}