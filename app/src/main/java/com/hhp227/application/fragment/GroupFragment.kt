package com.hhp227.application.fragment

import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.adapter.GroupGridAdapter
import com.hhp227.application.databinding.FragmentGroupBinding
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.GroupViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GroupFragment : Fragment() {
    private val viewModel: GroupViewModel by viewModels {
        InjectorUtils.provideGroupViewModelFactory()
    }

    private var binding: FragmentGroupBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireParentFragment().parentFragment as MainFragment).setNavAppbar(binding.toolbar)
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
        binding.rvGroup.apply {
            layoutManager = GridLayoutManager(context, when (resources.configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> PORTRAIT_SPAN_COUNT
                Configuration.ORIENTATION_LANDSCAPE -> LANDSCAPE_SPAN_COUNT
                else -> 0
            }).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int = if (binding.rvGroup.adapter?.getItemViewType(position) == TYPE_TEXT) spanCount else 1
                }
            }
            adapter = GroupGridAdapter().apply {
                setOnItemClickListener { _, i ->
                    (currentList[i] as? GroupItem.Group)?.also { groupItem ->
                        val directions = MainFragmentDirections.actionMainFragmentToGroupDetailFragment(groupItem)

                        requireActivity().findNavController(R.id.nav_host).navigate(directions)
                    }
                }
            }

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(RecyclerView.LAYOUT_DIRECTION_RTL)) {
                        viewModel.fetchNextPage()
                    }
                }
            })
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    super.getItemOffsets(outRect, view, parent, state)
                    val position = parent.getChildAdapterPosition(view)
                    val spanCount = (layoutManager as GridLayoutManager).spanCount

                    if (position > RecyclerView.NO_POSITION && (parent.adapter?.getItemViewType(position) == TYPE_GROUP || parent.adapter?.getItemViewType(position) == TYPE_AD)) {
                        outRect.apply {
                            top = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()
                            bottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics).toInt()
                            left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, if (position % spanCount == 1) 14f else 7f, resources.displayMetrics).toInt()
                            right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, if (position % spanCount == 0) 14f else 7f, resources.displayMetrics).toInt()
                        }
                    }
                }
            })
        }
        binding.srlGroup.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.srlGroup.isRefreshing = false

                viewModel.refreshGroupList()
            }, 1000)
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                when {
                    state.isLoading -> showProgressBar()
                    state.hasRequestedMore -> viewModel.fetchGroupList(state.offset)
                    state.itemList.isNotEmpty() -> {
                        hideProgressBar()
                        (binding.rvGroup.adapter as GroupGridAdapter).submitList(state.itemList)
                    }
                    state.error.isNotBlank() -> {
                        hideProgressBar()
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .launchIn(lifecycleScope)
        requireParentFragment().requireParentFragment().setFragmentResultListener("result1") { k, b ->
            viewModel.refreshGroupList()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        (binding.rvGroup.layoutManager as GridLayoutManager).spanCount = when (newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> PORTRAIT_SPAN_COUNT
            Configuration.ORIENTATION_LANDSCAPE -> LANDSCAPE_SPAN_COUNT
            else -> 0
        }

        binding.rvGroup.invalidateItemDecorations()
    }

    private fun showProgressBar() {
        if (binding.pbGroup.visibility == View.GONE)
            binding.pbGroup.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        if (binding.pbGroup.visibility == View.VISIBLE)
            binding.pbGroup.visibility = View.GONE
    }

    companion object {
        private const val PORTRAIT_SPAN_COUNT = 2
        private const val LANDSCAPE_SPAN_COUNT = 4
        private const val TYPE_TEXT = 0
        private const val TYPE_GROUP = 1
        private const val TYPE_AD = 2

        fun newInstance(): Fragment = GroupFragment()
    }
}