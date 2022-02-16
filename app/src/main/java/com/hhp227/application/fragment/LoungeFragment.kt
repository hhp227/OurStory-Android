package com.hhp227.application.fragment

import com.hhp227.application.fragment.PostFragment.Companion.POST_INFO_CODE
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.activity.MainActivity
import com.hhp227.application.activity.PostDetailActivity
import com.hhp227.application.activity.CreatePostActivity
import com.hhp227.application.adapter.PostListAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.data.PostRepository
import com.hhp227.application.databinding.FragmentMainBinding
import com.hhp227.application.dto.ListItem
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.LoungeViewModel
import com.hhp227.application.viewmodel.CreatePostViewModel.Companion.TYPE_INSERT
import com.hhp227.application.viewmodel.LoungeViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoungeFragment : Fragment() {
    private val viewModel: LoungeViewModel by viewModels {
        LoungeViewModelFactory(PostRepository())
    }

    private val writeActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.refreshPostList()
        }
    }

    private val postDetailActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        /*if (result.resultCode == RESULT_OK) {
            result.data?.let { intent ->
                val position = intent.getIntExtra("position", 0)
                viewModel.itemList[position] = intent.getParcelableExtra("post") ?: PostItem.Post()

                binding.recyclerView.adapter!!.notifyItemChanged(position)
            } ?: run {
                offset = 0

                binding.appBarLayout.setExpanded(true, false)
                viewModel.itemList.clear()
                fetchDataTask()
            }
        }*/
        if (result.resultCode == POST_INFO_CODE) {
            result.data?.also { intent ->
                viewModel.updatePost(intent.getParcelableExtra("post") ?: ListItem.Post())
            }
        } else if (result.resultCode == RESULT_OK) {
            viewModel.refreshPostList()
        }
    }

    private var binding: FragmentMainBinding by autoCleared()

    private var scrollListener: RecyclerView.OnScrollListener by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(RecyclerView.LAYOUT_DIRECTION_RTL)) {
                    viewModel.fetchNextPage()
                }
            }
        }

        (requireActivity() as MainActivity).setAppBar(binding.toolbar, getString(R.string.lounge_fragment))
        binding.recyclerView.apply {
            itemAnimator = null
            adapter = PostListAdapter().apply {
                setOnItemClickListener { v, p ->
                    (currentList[p] as ListItem.Post).also { post ->
                        val intent = Intent(context, PostDetailActivity::class.java)
                            .putExtra("post", post)
                            .putExtra("is_bottom", v.id == R.id.ll_reply)

                        postDetailActivityResultLauncher.launch(intent)
                    }
                }
            }

            addOnScrollListener(scrollListener)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipeRefreshLayout.isRefreshing = false

                viewModel.refreshPostList()
            }, 1000)
        }
        binding.fab.setOnClickListener {
            Intent(context, CreatePostActivity::class.java).also { intent ->
                intent.putExtra("type", TYPE_INSERT)
                writeActivityResultLauncher.launch(intent)
            }
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.offset == 0 -> Handler(Looper.getMainLooper()).postDelayed({
                    binding.appBarLayout.setExpanded(true, false)
                    binding.recyclerView.scrollToPosition(0)
                }, 500)
                state.itemList.isNotEmpty() -> {
                    hideProgressBar()
                    (binding.recyclerView.adapter as PostListAdapter).submitList(state.itemList)
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
            (binding.recyclerView.adapter as PostListAdapter).also { adapter ->
                adapter.currentList
                    .mapIndexed { index, post -> index to post }
                    .filter { (_, a) -> a is ListItem.Post && a.userId == AppController.getInstance().preferenceManager.user.id }
                    .forEach { (i, _) ->
                        if (adapter.currentList.isNotEmpty()) {
                            (adapter.currentList[i] as ListItem.Post).profileImage = AppController.getInstance().preferenceManager.user.profileImage

                            adapter.notifyItemChanged(i)
                        }
                    }
            }
        }
    }

    companion object {
        fun newInstance(): Fragment = LoungeFragment()
    }
}