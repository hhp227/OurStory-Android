package com.hhp227.application.fragment

import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.adapter.FriendListAdapter
import com.hhp227.application.databinding.FragmentFriendBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.FriendViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

class FriendFragment : Fragment() {
    private val viewModel: FriendViewModel by viewModels {
        InjectorUtils.provideFriendViewModelFactory()
    }

    private var binding: FragmentFriendBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireParentFragment().parentFragment as? MainFragment)?.setNavAppbar(binding.toolbar)
        binding.recyclerView.apply {
            adapter = FriendListAdapter().apply {
                setOnItemClickListener { user ->
                    Toast.makeText(requireContext(), "$user", Toast.LENGTH_LONG).show()
                }
            }

            addItemDecoration(FriendItemDecoration())
        }
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                when {
                    state.isLoading -> showProgressBar()
                    state.userItems.isNotEmpty() -> {
                        (binding.recyclerView.adapter as? FriendListAdapter)?.submitList(state.userItems)
                        hideProgressBar()
                    }
                    state.error.isNotBlank() -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                        hideProgressBar()
                    }
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun showProgressBar() {
        if (binding.progressBar.visibility == View.GONE)
            binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        if (binding.progressBar.visibility == View.VISIBLE)
            binding.progressBar.visibility = View.GONE
    }

    inner class FriendItemDecoration : RecyclerView.ItemDecoration() {
        private val mBounds = Rect()

        private var mDivider = requireContext().obtainStyledAttributes(intArrayOf(android.R.attr.listDivider)).getDrawable(0)

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDraw(c, parent, state)
            c.save()
            val left: Int
            val right: Int

            if (parent.clipToPadding) {
                left = parent.paddingLeft
                right = parent.width - parent.paddingRight

                c.clipRect(left, parent.paddingTop, right, parent.height - parent.paddingBottom)
            } else {
                left = 0
                right = parent.width
            }
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                parent.getDecoratedBoundsWithMargins(child, mBounds)
                val bottom: Int = mBounds.bottom + child.translationY.roundToInt()
                val top: Int = bottom - (mDivider?.intrinsicHeight ?: 0)

                mDivider?.setBounds(left, top, right, bottom)
                mDivider?.draw(c)
            }
            c.restore()
        }

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.apply {
                top = resources.getDimensionPixelOffset(R.dimen.friend_item_margin)
                bottom = resources.getDimensionPixelOffset(R.dimen.friend_item_margin)
                left = resources.getDimensionPixelOffset(R.dimen.friend_item_margin)
                right = resources.getDimensionPixelOffset(R.dimen.friend_item_margin)
            }
        }
    }
}