package com.hhp227.application.fragment

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.hhp227.application.R
import com.hhp227.application.adapter.ReplyListAdapter
import com.hhp227.application.databinding.FragmentPostDetailBinding
import com.hhp227.application.model.ListItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.CreatePostViewModel.Companion.TYPE_UPDATE
import com.hhp227.application.viewmodel.PostDetailViewModel
import com.hhp227.application.viewmodel.PostDetailViewModel.Companion.MAX_REPORT_COUNT
import kotlin.math.max

class PostDetailFragment : Fragment(), MenuProvider {
    private val viewModel: PostDetailViewModel by viewModels {
        InjectorUtils.providePostDetailViewModelFactory(this)
    }

    private val adapter = ReplyListAdapter()

    private var binding: FragmentPostDetailBinding by autoCleared()

    private lateinit var adapterDataObserver: AdapterDataObserver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.rvPost.adapter = adapter.apply { userId = viewModel.state.value?.user?.id ?: -1 }
        adapterDataObserver = object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart > 0 && viewModel.isScrollToLast) {
                    binding.rvPost.scrollToPosition(max(positionStart, itemCount))
                    viewModel.setScrollToLast(false)
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNavAppBar(binding.toolbar)
        adapter.registerAdapterDataObserver(adapterDataObserver)
        adapter.setOnImageClickListener { list, index ->
            val directions = PostDetailFragmentDirections.actionPostDetailFragmentToPictureFragment(list.toTypedArray(), index)

            findNavController().navigate(directions)
        }
        binding.srlPost.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.srlPost.isRefreshing = false

                viewModel.refresh()
            }, 1000)
        }
        binding.rvPost.addOnLayoutChangeListener { v, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom && adapter.itemCount > 1) {
                binding.rvPost.post { (v as RecyclerView).scrollToPosition(adapter.itemCount - 1) }
            }
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when {
                state.textError != null -> Toast.makeText(requireContext(), getString(state.textError), Toast.LENGTH_LONG).show()
                state.replyId >= 0 -> {
                    binding.etReply.setText("")
                    viewModel.fetchReply(state.replyId)
                    Toast.makeText(requireContext(), getString(R.string.send_complete), Toast.LENGTH_LONG).show()
                    (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(binding.cvBtnSend.windowToken, 0)

                    // 전송하면 리스트 하단으로 이동
                    viewModel.setScrollToLast(true)
                }
                state.isSetResultOK -> if (findNavController().currentDestination?.id == R.id.postDetailFragment) {
                    setFragmentResult(findNavController().previousBackStackEntry?.destination?.displayName ?: "", bundleOf("post" to viewModel.post))
                    findNavController().navigateUp()
                    Toast.makeText(requireContext(), if (viewModel.post.reportCount > MAX_REPORT_COUNT) getString(R.string.reported_post) else getString(R.string.delete_complete), Toast.LENGTH_LONG).show()
                }
            }
        }
        setFragmentResultListener(findNavController().currentDestination?.displayName ?: "") { _, _ ->
            viewModel.refresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity).removeMenuProvider(this)
        adapter.unregisterAdapterDataObserver(adapterDataObserver)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        1000 -> {
            adapter.currentList
                .let { list ->
                    if (list[item.groupId] is ListItem.Post) (list[item.groupId] as ListItem.Post).text
                    else (list[item.groupId] as ListItem.Reply).reply
                }
                .also { text ->
                    (requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(null, text))
                }
            Toast.makeText(requireContext(), "클립보드에 복사되었습니다!", Toast.LENGTH_LONG).show()
            true
        }
        1001 -> {
            (adapter.currentList[item.groupId] as? ListItem.Reply)?.also { reply ->
                val directions = PostDetailFragmentDirections.actionPostDetailFragmentToUpdateReplyFragment(reply)

                findNavController().navigate(directions)
            }
            true
        }
        1002 -> {
            viewModel.deleteReply(adapter.currentList[item.groupId] as? ListItem.Reply ?: ListItem.Reply())
            true
        }
        else -> super.onContextItemSelected(item)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        val userId = viewModel.state.value?.user?.id ?: -1

        menuInflater.inflate(if (userId == viewModel.post.userId) R.menu.my_post else R.menu.other_post, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.edit -> {
            (adapter.currentList[0] as? ListItem.Post)?.also { post ->
                val directions = PostDetailFragmentDirections.actionPostDetailFragmentToCreatePostFragment(TYPE_UPDATE, 0, post)

                findNavController().navigate(directions)
            }
            true
        }
        R.id.delete -> {
            showAlertDialog(getString(R.string.delete_title), getString(R.string.delete_message), viewModel::deletePost)
            true
        }
        R.id.report -> {
            showAlertDialog(getString(R.string.report_title), getString(R.string.report_message), viewModel::togglePostReport)
            true
        }
        R.id.block -> {
            showAlertDialog(getString(R.string.block_title), getString(R.string.block_message), viewModel::toggleUserBlocking)
            true
        }
        else -> false
    }

    private fun setNavAppBar(toolbar: Toolbar) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).addMenuProvider(this)
        toolbar.setupWithNavController(findNavController())
    }

    private fun showAlertDialog(title: String, message: String, action: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(android.R.string.ok)) { dialogInterface, _ ->
                action()
                dialogInterface.dismiss()
            }
            .setNegativeButton(getString(android.R.string.cancel)) { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()
            .show()
    }
}