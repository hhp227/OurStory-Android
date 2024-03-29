package com.hhp227.application.fragment

import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.adapter.FriendListAdapter
import com.hhp227.application.databinding.FragmentFriendBinding
import com.hhp227.application.databinding.MenuSearchBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.FriendViewModel
import kotlin.math.roundToInt

// 한번더 체크할것
class FriendFragment : Fragment(), MenuProvider {
    private val viewModel: FriendViewModel by viewModels {
        InjectorUtils.provideFriendViewModelFactory()
    }

    private var binding: FragmentFriendBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFriendBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.recyclerView.adapter = FriendListAdapter().apply {
            setOnItemClickListener { user ->
                Toast.makeText(requireContext(), "$user", Toast.LENGTH_LONG).show()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireParentFragment().parentFragment as? MainFragment)?.setNavAppbar(binding.toolbar)
        (requireActivity() as AppCompatActivity).addMenuProvider(this)
        binding.recyclerView.addItemDecoration(FriendItemDecoration())
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when {
                state.error.isNotBlank() -> {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity).removeMenuProvider(this)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.search, menu)
        if (menu.hasVisibleItems()) {
            menu.findItem(R.id.search).actionView = MenuSearchBinding.inflate(layoutInflater).run {
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        Toast.makeText(requireContext(), "query: $query", Toast.LENGTH_LONG).show()
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return false
                    }
                })
                return@run root
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem) = false

    inner class FriendItemDecoration : RecyclerView.ItemDecoration() {
        private val bounds = Rect()

        private var divider = requireContext().obtainStyledAttributes(intArrayOf(android.R.attr.listDivider)).getDrawable(0)

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
                parent.getDecoratedBoundsWithMargins(child, bounds)
                val bottom: Int = bounds.bottom + child.translationY.roundToInt()
                val top: Int = bottom - (divider?.intrinsicHeight ?: 0)

                divider?.setBounds(left, top, right, bottom)
                divider?.draw(c)
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