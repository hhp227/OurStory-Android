package com.hhp227.application.fragment

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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.adapter.MemberGridAdapter
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.MemberViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// WIP
class MemberFragment : Fragment() {
    private val viewModel: MemberViewModel by viewModels {
        InjectorUtils.provideMemberViewModelFactory(this)
    }

    private var binding: FragmentTabBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), SPAN_COUNT)
            adapter = MemberGridAdapter().apply {
                setOnItemClickListener { _, p ->
                    val user = currentList[p]
                    val directions = GroupDetailFragmentDirections.actionGroupDetailFragmentToUserFragment(user)

                    findNavController().navigate(directions)
                }
            }

            addOnScrollListener(object : RecyclerView.OnScrollListener() {

            })
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                delay(1000)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when {
                state.isLoading -> showProgressBar()
                state.users.isNotEmpty() -> {
                    hideProgressBar()
                    (binding.recyclerView.adapter as MemberGridAdapter).submitList(state.users)
                }
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.userFlow
            .onEach { user ->
                (binding.recyclerView.adapter as? MemberGridAdapter)?.also { adapter ->
                    adapter.currentList
                        .find { it.id == user?.id }
                        .let(adapter.currentList::indexOf)
                        .also { i ->
                            if (i >= 0) {
                                adapter.currentList[i].profileImage = user?.profileImage

                                adapter.notifyItemChanged(i)
                            }
                        }
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.run { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.run { visibility = View.GONE }

    companion object {
        private const val SPAN_COUNT = 4
        private const val ARG_PARAM1 = "group_id"

        fun newInstance(groupId: Int) =
            MemberFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, groupId)
                }
            }
    }
}