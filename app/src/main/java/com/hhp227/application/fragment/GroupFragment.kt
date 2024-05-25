package com.hhp227.application.fragment

import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.adapter.GroupGridAdapter
import com.hhp227.application.adapter.GroupGridAdapter.Companion.TYPE_AD
import com.hhp227.application.adapter.GroupGridAdapter.Companion.TYPE_GROUP
import com.hhp227.application.adapter.ItemLoadStateAdapter
import com.hhp227.application.databinding.FragmentGroupBinding
import com.hhp227.application.model.GroupItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.GroupViewModel

class GroupFragment : Fragment() {
    private val viewModel: GroupViewModel by viewModels {
        InjectorUtils.provideGroupViewModelFactory()
    }

    private val adapter = GroupGridAdapter()

    private var binding: FragmentGroupBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGroupBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.rvGroup.adapter = adapter.withLoadStateFooter(ItemLoadStateAdapter(adapter::retry))
        binding.spanCount = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> PORTRAIT_SPAN_COUNT
            Configuration.ORIENTATION_LANDSCAPE -> LANDSCAPE_SPAN_COUNT
            else -> 0
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireParentFragment().parentFragment as MainFragment).setNavAppbar(binding.toolbar)
        binding.srlGroup.setOnRefreshListener(adapter::refresh)
        binding.bnvGroupButton.apply {
            menu.getItem(0).isCheckable = false

            setOnItemSelectedListener {
                it.isCheckable = false
                return@setOnItemSelectedListener requireActivity().findNavController(R.id.nav_host)
                    .let { navController ->
                        when (it.itemId) {
                            R.id.navigationFind -> {
                                navController.navigate(R.id.findGroupFragment)
                                true
                            }
                            R.id.navigationRequest -> {
                                navController.navigate(R.id.joinRequestGroupFragment)
                                true
                            }
                            R.id.navigationCreate -> {
                                navController.navigate(R.id.createGroupFragment)
                                true
                            }
                            else -> false
                        }
                    }
            }
        }
        adapter.setOnItemClickListener { _, i ->
            (adapter.snapshot()[i] as? GroupItem.Group)?.also { groupItem ->
                val directions = MainFragmentDirections.actionMainFragmentToGroupDetailFragment(groupItem)

                requireActivity().findNavController(R.id.nav_host).navigate(directions)
            }
        }
        //binding.rvGroup.addItemDecoration(itemDecoration)
        requireParentFragment().requireParentFragment().setFragmentResultListener("result1") { k, b ->
            //viewModel.refreshGroupList()
        }
        subscribeUi()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.spanCount = when (newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> PORTRAIT_SPAN_COUNT
            Configuration.ORIENTATION_LANDSCAPE -> LANDSCAPE_SPAN_COUNT
            else -> 0
        }
    }

    private fun subscribeUi() {
        adapter.loadState.observe(viewLifecycleOwner) {
            binding.srlGroup.isRefreshing = it.mediator?.refresh is LoadState.Loading
            binding.isLoading = it.refresh is LoadState.Loading
            binding.isEmpty = it.refresh is LoadState.NotLoading && adapter.itemCount == 0
        }
    }

    inner class GroupGridItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            val position = parent.getChildAdapterPosition(view)
            val spanCount = (parent.layoutManager as GridLayoutManager).spanCount

            if (position > RecyclerView.NO_POSITION &&
                (parent.adapter?.getItemViewType(position) == TYPE_GROUP ||
                        parent.adapter?.getItemViewType(position) == TYPE_AD)) {
                outRect.apply {
                    top = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()
                    bottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics).toInt()
                    left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, if (position % spanCount == 1) 14f else 7f, resources.displayMetrics).toInt()
                    right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, if (position % spanCount == 0) 14f else 7f, resources.displayMetrics).toInt()
                }
            }
        }
    }

    companion object {
        private const val PORTRAIT_SPAN_COUNT = 2
        private const val LANDSCAPE_SPAN_COUNT = 4
    }
}
