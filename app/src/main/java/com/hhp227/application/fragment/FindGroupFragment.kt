package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.adapter.GroupListPagingAdapter
import com.hhp227.application.adapter.ItemLoadStateAdapter
import com.hhp227.application.databinding.FragmentGroupFindBinding
import com.hhp227.application.model.GroupItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.FindGroupViewModel

class FindGroupFragment : Fragment() {
    private var binding: FragmentGroupFindBinding by autoCleared()

    private val viewModel: FindGroupViewModel by viewModels {
        InjectorUtils.provideFindGroupViewModelFactory()
    }

    private val adapter = GroupListPagingAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGroupFindBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.recyclerView.adapter = adapter.withLoadStateFooter(ItemLoadStateAdapter(adapter::retry))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setupWithNavController(findNavController())
        binding.swipeRefreshLayout.setOnRefreshListener(adapter::refresh)
        adapter.setOnItemClickListener { _, position ->
            if (position != RecyclerView.NO_POSITION) {
                val groupItem = adapter.snapshot()[position] as GroupItem.Group
                val directions = FindGroupFragmentDirections.actionFindGroupFragmentToGroupInfoFragment(groupItem)

                findNavController().navigate(directions)
            }
        }
        adapter.loadState.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it.mediator?.refresh is LoadState.Loading
            binding.isLoading = it.refresh is LoadState.Loading
        }
    }
}
