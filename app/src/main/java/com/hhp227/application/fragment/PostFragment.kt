package com.hhp227.application.fragment

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
import com.hhp227.application.activity.PostDetailActivity
import com.hhp227.application.adapter.PostListAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.data.PostRepository
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.dto.ListItem
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.PostViewModel
import com.hhp227.application.viewmodel.PostViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostFragment : Fragment() {
    private val viewModel: PostViewModel by viewModels {
        PostViewModelFactory(PostRepository(), this, arguments)
    }

    private val postDetailActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        parentFragmentManager.fragments.forEach { fragment ->
            when (fragment) {
                is PostFragment -> fragment.onPostDetailActivityResult(result)
                is AlbumFragment -> fragment.onPostDetailActivityResult(result)
            }
        }
    }

    private var binding: FragmentTabBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            adapter = PostListAdapter().apply {
                setOnItemClickListener(object : PostListAdapter.OnItemClickListener {
                    override fun onItemClick(v: View, p: Int) {
                        (currentList[p] as ListItem.Post).also { post ->
                            val intent = Intent(requireContext(), PostDetailActivity::class.java)
                                .putExtra("post", post)
                                .putExtra("is_bottom", v.id == R.id.ll_reply)
                                .putExtra("group_name", viewModel.groupName)

                            postDetailActivityResultLauncher.launch(intent)
                        }
                    }

                    override fun onLikeClick(p: Int) {
                        (currentList[p] as? ListItem.Post)?.also { post ->
                            viewModel.togglePostLike(post)
                        }
                    }
                })
            }

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(RecyclerView.LAYOUT_DIRECTION_RTL)) {
                        viewModel.fetchNextPage()
                    }
                }
            })
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                delay(1000)
                binding.swipeRefreshLayout.isRefreshing = false

                viewModel.refreshPostList()
            }
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.offset == 0 -> Handler(Looper.getMainLooper()).postDelayed({
                    (parentFragment as? TabHostLayoutFragment)?.appbarLayoutExpand()
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
        /*if (viewModel.postItems.size < 2) {
            viewModel.postItems.add(0, PostItem.Empty(R.drawable.ic_baseline_library_add_72, getString(R.string.add_message)))
        }*/
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    fun onWriteActivityResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            viewModel.refreshPostList()
        }
    }

    fun onMyInfoActivityResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            (binding.recyclerView.adapter as PostListAdapter).also { adapter ->
                adapter.currentList
                    .mapIndexed { index, post -> index to post }
                    .filter { (_, a) -> a is ListItem.Post && a.userId == AppController.getInstance().preferenceManager.user?.id }
                    .forEach { (i, _) ->
                        if (adapter.currentList.isNotEmpty()) {
                            (adapter.currentList[i] as ListItem.Post).profileImage = AppController.getInstance().preferenceManager.user?.profileImage

                            adapter.notifyItemChanged(i)
                        }
                    }
            }
        }
    }

    fun onPostDetailActivityResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            result.data
                ?.also { intent -> viewModel.updatePost(intent.getParcelableExtra("post") ?: ListItem.Post()) }
                ?: viewModel.refreshPostList()
        }
    }

    companion object {
        private const val ARG_PARAM1 = "group_id"
        private const val ARG_PARAM2 = "group_name"

        fun newInstance(groupId: Int, groupName: String) =
            PostFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, groupId)
                    putString(ARG_PARAM2, groupName)
                }
            }
    }
}