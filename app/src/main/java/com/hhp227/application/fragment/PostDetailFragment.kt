package com.hhp227.application.fragment

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.adapter.ReplyListAdapter
import com.hhp227.application.databinding.FragmentPostDetailBinding
import com.hhp227.application.model.ListItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.CreatePostViewModel.Companion.TYPE_UPDATE
import com.hhp227.application.viewmodel.PostDetailViewModel
import com.hhp227.application.viewmodel.PostDetailViewModel.Companion.MAX_REPORT_COUNT
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PostDetailFragment : Fragment() {
    private var binding: FragmentPostDetailBinding by autoCleared()

    private val viewModel: PostDetailViewModel by viewModels {
        InjectorUtils.providePostDetailViewModelFactory(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.apply {
            title = if (TextUtils.isEmpty(viewModel.groupName)) getString(R.string.lounge_fragment) else viewModel.groupName

            setupWithNavController(findNavController())
            inflateMenu(if (viewModel.isAuth) R.menu.my_post else R.menu.other_post)
            setOnMenuItemClickListener(::onOptionsItemSelected)
        }
        binding.srlPost.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.srlPost.isRefreshing = false

                viewModel.refreshPostList()
            }, 1000)
        }
        binding.rvPost.apply {
            adapter = ReplyListAdapter()

            addOnLayoutChangeListener { view, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom && (adapter as ReplyListAdapter).itemCount > 1) {
                    post { (view as RecyclerView).scrollToPosition((adapter as ReplyListAdapter).itemCount - 1) }
                }
            }
        }
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { state ->
                when {
                    state.textError != null -> Toast.makeText(requireContext(), getString(state.textError), Toast.LENGTH_LONG).show()
                    state.replyId >= 0 -> {
                        Toast.makeText(requireContext(), getString(R.string.send_complete), Toast.LENGTH_LONG).show()

                        // 전송할때마다 하단으로
                        viewModel.setScrollToLast(true)
                        binding.etReply.setText("")
                        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(binding.cvBtnSend.windowToken, 0)
                    }
                    state.isSetResultOK -> if (findNavController().currentDestination?.id == R.id.postDetailFragment) {
                        setFragmentResult(findNavController().previousBackStackEntry?.destination?.displayName ?: "", bundleOf())
                        findNavController().navigateUp()
                        Toast.makeText(requireContext(), if (viewModel.post.reportCount > MAX_REPORT_COUNT) getString(R.string.reported_post) else getString(R.string.delete_complete), Toast.LENGTH_LONG).show()
                    }
                }
            }
            .launchIn(lifecycleScope)
        viewModel.user.observe(viewLifecycleOwner) { user ->
            (binding.rvPost.adapter as ReplyListAdapter).apply {
                setOnItemLongClickListener { v, p ->
                    v.setOnCreateContextMenuListener { menu, _, _ ->
                        menu.apply {
                            setHeaderTitle(v.context.getString(R.string.select_action))
                            add(0, p, Menu.NONE, v.context.getString(R.string.copy_content))
                            if (currentList[p] is ListItem.Reply) {
                                if ((currentList[p] as ListItem.Reply).userId == user?.id) {
                                    add(1, p, Menu.NONE, v.context.getString(R.string.edit_comment))
                                    add(2, p, Menu.NONE, v.context.getString(R.string.delete_comment))
                                }
                            }
                        }
                    }
                    v.showContextMenu()
                }
            }
        }
        viewModel.isScrollToLastFlow
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { isScrollToLast ->
                if (isScrollToLast) {
                    delay(300)
                    binding.rvPost.scrollToPosition(binding.rvPost.adapter?.itemCount?.minus(1) ?: 0)
                    viewModel.setScrollToLast(false)
                }
            }
            .launchIn(lifecycleScope)
        viewModel.postState
            .observe(viewLifecycleOwner) { post ->
            if (post != viewModel.post) {
                setFragmentResult(findNavController().previousBackStackEntry?.destination?.displayName ?: "", bundleOf("post" to post))
            }
        }
        setFragmentResultListener(findNavController().currentDestination?.displayName ?: "") { _, b ->
            b.getParcelable<ListItem.Reply>("reply")
                ?.also(viewModel::updateReply)
                ?: viewModel.refreshPostList()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.edit -> {
            ((binding.rvPost.adapter as ReplyListAdapter).currentList[0] as? ListItem.Post)?.also { post ->
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
        else -> super.onOptionsItemSelected(item)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.groupId) {
        0 -> {
            (binding.rvPost.adapter as ReplyListAdapter).currentList.let { list ->
                if (list[item.itemId] is ListItem.Post) (list[item.itemId] as ListItem.Post).text else (list[item.itemId] as ListItem.Reply).reply
            }.also { text ->
                (requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(null, text))
            }
            Toast.makeText(requireContext(), "클립보드에 복사되었습니다!", Toast.LENGTH_LONG).show()
            true
        }
        1 -> {
            ((binding.rvPost.adapter as ReplyListAdapter).currentList[item.itemId] as? ListItem.Reply)?.also { reply ->
                val directions = PostDetailFragmentDirections.actionPostDetailFragmentToUpdateReplyFragment(reply)

                findNavController().navigate(directions)
            }
            true
        }
        2 -> {
            viewModel.deleteReply(viewModel.state.value.itemList[item.itemId] as? ListItem.Reply ?: ListItem.Reply())
            true
        }
        else -> super.onContextItemSelected(item)
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