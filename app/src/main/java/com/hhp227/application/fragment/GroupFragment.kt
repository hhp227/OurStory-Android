package com.hhp227.application.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.R
import com.hhp227.application.activity.*
import com.hhp227.application.adapter.GroupGridAdapter
import com.hhp227.application.databinding.*
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.GroupViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GroupFragment : Fragment() {
    private val viewModel: GroupViewModel by viewModels {
        InjectorUtils.provideGroupViewModelFactory()
    }

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.refreshGroupList()
        }
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

                when (it.itemId) {
                    R.id.navigationFind -> {
                        activityResultLauncher.launch(Intent(context, FindGroupActivity::class.java))
                        true
                    }
                    R.id.navigationRequest -> {
                        startActivity(Intent(context, JoinRequestGroupActivity::class.java))
                        true
                    }
                    R.id.navigationCreate -> {
                        activityResultLauncher.launch(Intent(context, CreateGroupActivity::class.java))
                        true
                    }
                    else -> false
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
                        Intent(context, GroupActivity::class.java)
                            .putExtra("group", groupItem)
                            .also(activityResultLauncher::launch)
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
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            /*when {
                state.isLoading -> showProgressBar()
                state.itemList.isNotEmpty() -> {
                    hideProgressBar()
                    (binding.rvGroup.adapter as GroupGridAdapter).submitList(state.itemList)
                }
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }*/
            if (state.itemList.isNotEmpty()) {
                (binding.rvGroup.adapter as GroupGridAdapter).submitList(state.itemList)
            }
            Log.e("TEST", "GroupFragment $state")
        }.launchIn(lifecycleScope)
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