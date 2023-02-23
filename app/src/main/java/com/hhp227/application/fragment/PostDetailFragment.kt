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
import com.hhp227.application.R
import com.hhp227.application.adapter.ReplyListAdapter
import com.hhp227.application.databinding.FragmentPostDetailBinding
import com.hhp227.application.model.ListItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.CreatePostViewModel.Companion.TYPE_UPDATE
import com.hhp227.application.viewmodel.PostDetailViewModel
import com.hhp227.application.viewmodel.PostDetailViewModel.Companion.MAX_REPORT_COUNT

class PostDetailFragment : Fragment(), MenuProvider {
    private val viewModel: PostDetailViewModel by viewModels {
        InjectorUtils.providePostDetailViewModelFactory(this)
    }

    private val adapter = ReplyListAdapter()

    private var binding: FragmentPostDetailBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.onValueChange = fun(text: Any?) = with(viewModel) { state.value = state.value?.copy(reply = text.toString(), textError = null, replyId = -1) }
        binding.rvPost.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNavAppBar(binding.toolbar)
        binding.srlPost.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.srlPost.isRefreshing = false

                viewModel.refreshPostList()
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

                    // 전송할때마다 하단으로
                    viewModel.setScrollToLast(true)
                }
                state.isSetResultOK -> if (findNavController().currentDestination?.id == R.id.postDetailFragment) {
                    setFragmentResult(findNavController().previousBackStackEntry?.destination?.displayName ?: "", bundleOf())
                    findNavController().navigateUp()
                    Toast.makeText(requireContext(), if (viewModel.post.reportCount > MAX_REPORT_COUNT) getString(R.string.reported_post) else getString(R.string.delete_complete), Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.isScrollToLast.observe(viewLifecycleOwner) { isScrollToLast ->
            if (isScrollToLast) {
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.rvPost.scrollToPosition(binding.rvPost.adapter?.itemCount?.minus(1) ?: 0)
                    viewModel.setScrollToLast(false)
                }, 300)
            }
        }
        /*viewModel.postState.observe(viewLifecycleOwner) { post ->
            if (post != viewModel.post) {
                setFragmentResult(findNavController().previousBackStackEntry?.destination?.displayName ?: "", bundleOf("post" to post))
            }
        }*/
        setFragmentResultListener(findNavController().currentDestination?.displayName ?: "") { _, _ ->
            /*b.getParcelable<ListItem.Reply>("reply")
                ?.also(viewModel::updateReply)
                ?: viewModel.refreshPostList()*/
            viewModel.refreshPostList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity).removeMenuProvider(this)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.groupId) {
        0 -> {
            adapter.currentList.let { list ->
                if (list[item.itemId] is ListItem.Post) (list[item.itemId] as ListItem.Post).text else (list[item.itemId] as ListItem.Reply).reply
            }.also { text ->
                (requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(null, text))
            }
            Toast.makeText(requireContext(), "클립보드에 복사되었습니다!", Toast.LENGTH_LONG).show()
            true
        }
        1 -> {
            (adapter.currentList[item.itemId] as? ListItem.Reply)?.also { reply ->
                val directions = PostDetailFragmentDirections.actionPostDetailFragmentToUpdateReplyFragment(reply)

                findNavController().navigate(directions)
            }
            true
        }
        2 -> {
            viewModel.deleteReply(adapter.currentList[item.itemId] as? ListItem.Reply ?: ListItem.Reply())
            true
        }
        else -> super.onContextItemSelected(item)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            menuInflater.inflate(if (user?.id == viewModel.post.userId) R.menu.my_post else R.menu.other_post, menu)
            adapter.setOnItemLongClickListener { v, p ->
                v.setOnCreateContextMenuListener { contextMenu, _, _ ->
                    contextMenu.apply {
                        setHeaderTitle(v.context.getString(R.string.select_action))
                        add(0, p, Menu.NONE, v.context.getString(R.string.copy_content))
                        if (adapter.currentList[p] is ListItem.Reply) {
                            if ((adapter.currentList[p] as ListItem.Reply).userId == user?.id) {
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