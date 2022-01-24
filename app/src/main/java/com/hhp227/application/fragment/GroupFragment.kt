package com.hhp227.application.fragment

import android.app.Activity
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
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
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
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.GroupViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GroupFragment : Fragment() {
    private val viewModel: GroupViewModel by viewModels()

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.getGroupList()
        }
    }

    private var binding: FragmentGroupBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.spanCount = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> PORTRAIT_SPAN_COUNT
            Configuration.ORIENTATION_LANDSCAPE -> LANDSCAPE_SPAN_COUNT
            else -> 0
        }

        (requireActivity() as? AppCompatActivity)?.run {
            title = getString(R.string.group_fragment)

            setSupportActionBar(binding.toolbar)
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.itemList.isNotEmpty() -> {
                    hideProgressBar()
                    (binding.rvGroup.adapter as GroupGridAdapter).submitList(state.itemList)
                }
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
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
                        startActivity(Intent(context, NotJoinedGroupActivity::class.java))
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
            layoutManager = GridLayoutManager(context, viewModel.spanCount).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int = if (binding.rvGroup.adapter!!.getItemViewType(position) == TYPE_TEXT) spanCount else 1
                }
            }
            adapter = GroupGridAdapter().apply {
                setOnItemClickListener { _, i ->
                    (currentList[i] as? GroupItem)?.also { groupItem ->
                        Intent(context, GroupActivity::class.java)
                            .putExtra("group_id", groupItem.id)
                            .putExtra("author_id", groupItem.authorId)
                            .putExtra("group_name", groupItem.groupName)
                            .also(activityResultLauncher::launch)
                    }
                }
            }

            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    super.getItemOffsets(outRect, view, parent, state)
                    val position = parent.getChildAdapterPosition(view)

                    if (position > -1 && (parent.adapter?.getItemViewType(position) == TYPE_GROUP || parent.adapter?.getItemViewType(position) == TYPE_AD)) {
                        outRect.apply {
                            top = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()
                            bottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics).toInt()
                            left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, if (position % viewModel.spanCount == 1) 14f else 7f, resources.displayMetrics).toInt()
                            right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, if (position % viewModel.spanCount == 0) 14f else 7f, resources.displayMetrics).toInt()
                        }
                    }
                }
            })
        }
        binding.srlGroup.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.srlGroup.isRefreshing = false

                viewModel.getGroupList()
            }, 1000)
        }
        setDrawerToggle()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewModel.spanCount = when (newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> PORTRAIT_SPAN_COUNT
            Configuration.ORIENTATION_LANDSCAPE -> LANDSCAPE_SPAN_COUNT
            else -> 0
        }
        (binding.rvGroup.layoutManager as GridLayoutManager).spanCount = viewModel.spanCount

        binding.rvGroup.invalidateItemDecorations()
    }

    private fun setDrawerToggle() {
        val activityMainBinding = (requireActivity() as MainActivity).binding

        ActionBarDrawerToggle(requireActivity(), activityMainBinding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close).let {
            activityMainBinding.drawerLayout.addDrawerListener(it)
            it.syncState()
        }
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
        const val CREATE_CODE = 10
        const val REGISTER_CODE = 20
        const val UPDATE_CODE = 30
        private const val PORTRAIT_SPAN_COUNT = 2
        private const val LANDSCAPE_SPAN_COUNT = 4
        private const val TYPE_TEXT = 0
        private const val TYPE_GROUP = 1
        private const val TYPE_AD = 2
        private val TAG = GroupFragment::class.java.simpleName

        fun newInstance(): Fragment = GroupFragment()
    }
}