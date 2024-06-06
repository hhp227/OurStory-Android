package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hhp227.application.adapter.AlbumPagingAdapter
import com.hhp227.application.adapter.ItemLoadStateAdapter
import com.hhp227.application.databinding.FragmentAlbumBinding
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.ListItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.AlbumViewModel

class AlbumFragment : Fragment() {
    private val viewModel: AlbumViewModel by viewModels {
        InjectorUtils.provideAlbumViewModelFactory(this)
    }

    private var binding: FragmentAlbumBinding by autoCleared()

    private val adapter = AlbumPagingAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAlbumBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.recyclerView.adapter = adapter.withLoadStateFooter(ItemLoadStateAdapter(adapter::retry))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefreshLayout.setOnRefreshListener(::refresh)
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when {
                state.message?.isNotBlank() == true -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    //(binding.recyclerView.adapter as PostGridAdapter).submitList(listOf(ListItem.Empty(R.drawable.ic_baseline_library_add_72, getString(R.string.add_message))))
                }
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