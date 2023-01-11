package com.hhp227.application.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.adapter.ItemLoadStateAdapter
import com.hhp227.application.adapter.PostListAdapter
import com.hhp227.application.adapter.PostPagingDataAdapter
import com.hhp227.application.databinding.FragmentLoungeBinding
import com.hhp227.application.model.ListItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.CreatePostViewModel.Companion.TYPE_INSERT
import com.hhp227.application.viewmodel.LoungeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LoungeFragment : Fragment() {
    private val viewModel: LoungeViewModel by viewModels {
        InjectorUtils.provideLoungeViewModelFactory()
    }

    private val adapter = PostPagingDataAdapter()

    private var binding: FragmentLoungeBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoungeBinding.inflate(inflater, container, false)
        binding.recyclerView.adapter = adapter.withLoadStateFooter(ItemLoadStateAdapter(adapter::retry))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireParentFragment().parentFragment as? MainFragment)?.setNavAppbar(binding.toolbar)
        adapter.setOnItemClickListener(object : PostPagingDataAdapter.OnItemClickListener {
            override fun onItemClick(v: View, p: Int) {
                adapter.snapshot().items[p].also { post ->
                    val directions = MainFragmentDirections.actionMainFragmentToPostDetailFragment(post, v.id == R.id.ll_reply, null)

                    requireActivity().findNavController(R.id.nav_host).navigate(directions)
                }
            }

            override fun onLikeClick(p: Int) {
                /*adapter.snapshot()[p]?.also { post ->
                    viewModel.togglePostLike(post)
                }*/
            }
        })
        binding.fab.setOnClickListener {
            val directions = MainFragmentDirections.actionMainFragmentToCreatePostFragment(TYPE_INSERT, 0)

            requireActivity().findNavController(R.id.nav_host).navigate(directions)
        }
        viewModel.posts.observe(viewLifecycleOwner) {
            adapter.submitData(lifecycle, it)
        }
        viewModel.userFlow
            .onEach { user ->
                if (user != null) {
                    adapter.snapshot()
                        .mapIndexed { index, post -> index to post }
                        .filter { (_, a) -> a is ListItem.Post && a.userId == user.id }
                        .forEach { (i, _) ->
                            if (adapter.snapshot().isNotEmpty()) {
                                (adapter.snapshot()[i] as ListItem.Post).profileImage = user.profileImage

                                adapter.notifyItemChanged(i)
                            }
                        }
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.run { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.run { visibility = View.GONE }

    fun onFragmentResult(bundle: Bundle) {
        bundle.getParcelable<ListItem.Post>("post")
            ?.also(viewModel::updatePost)
            ?: viewModel.refreshPostList()
    }
}