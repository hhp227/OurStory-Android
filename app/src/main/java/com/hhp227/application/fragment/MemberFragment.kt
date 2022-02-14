package com.hhp227.application.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.adapter.MemberGridAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.data.UserRepository
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.MemberViewModel
import com.hhp227.application.viewmodel.MemberViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MemberFragment : Fragment() {
    private val viewModel: MemberViewModel by viewModels {
        MemberViewModelFactory(UserRepository(), this, arguments)
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
                    val newFragment = UserFragment.newInstance().apply {
                        arguments = Bundle().apply {
                            putParcelable("user", user)
                        }
                    }

                    newFragment.show(childFragmentManager, "dialog")
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
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.userItems.isNotEmpty() -> {
                    hideProgressBar()
                    (binding.recyclerView.adapter as MemberGridAdapter).submitList(state.userItems)
                }
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    fun onMyInfoActivityResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            with(AppController.getInstance().preferenceManager) {
                (binding.recyclerView.adapter as? MemberGridAdapter)?.also { adapter ->
                    adapter.currentList.find { it.id == user.id }
                        .let(viewModel.state.value.userItems::indexOf)
                        .also { i ->
                            adapter.currentList[i].profileImage = user.profileImage

                            adapter.notifyItemChanged(i)
                        }
                }
            }
        }
    }

    companion object {
        private const val SPAN_COUNT = 4
        private const val ARG_PARAM1 = "group_id"
        private val TAG = MemberFragment::class.java.simpleName

        fun newInstance(groupId: Int) =
            MemberFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, groupId)
                }
            }
    }
}