package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.hhp227.application.adapter.ItemLoadStateAdapter
import com.hhp227.application.adapter.MemberPagingAdapter
import com.hhp227.application.databinding.FragmentMemberBinding
import com.hhp227.application.model.GroupItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.MemberViewModel
import java.util.function.Function

class MemberFragment : Fragment() {
    private val viewModel: MemberViewModel by viewModels {
        InjectorUtils.provideMemberViewModelFactory(this)
    }

    private var binding: FragmentMemberBinding by autoCleared()

    private val adapter = MemberPagingAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMemberBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.recyclerView.adapter = adapter.withLoadStateFooter(ItemLoadStateAdapter(adapter::retry))
        binding.spanCount = 4
        binding.onSpanSizeListener = Function { position ->
            if (position == adapter.itemCount) binding.spanCount else 1
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefreshLayout.setOnRefreshListener(::refresh)
        adapter.setOnItemClickListener { _, p ->
            val user = adapter.snapshot().items[p]
            val directions = GroupDetailFragmentDirections.actionGroupDetailFragmentToUserFragment(user)

            findNavController().navigate(directions)
        }
        adapter.loadState.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it.mediator?.refresh is LoadState.Loading

            viewModel.setLoading(it.refresh is LoadState.Loading)
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.user != null) {
                adapter.updateProfileImages(state.user)
            }
        }
    }

    private fun refresh() {
        viewModel.refresh()
        adapter.refresh()
    }

    companion object {
        private const val ARG_PARAM = "group"

        fun newInstance(group: GroupItem.Group) =
            MemberFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM, group)
                }
            }
    }
}