package com.hhp227.application.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hhp227.application.R
import com.hhp227.application.adapter.ReplyListAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.databinding.ActivityPostDetailBinding
import com.hhp227.application.dto.ListItem
import com.hhp227.application.fragment.PostFragment
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.viewmodel.CreatePostViewModel.Companion.TYPE_UPDATE
import com.hhp227.application.viewmodel.PostDetailViewModel
import com.hhp227.application.viewmodel.PostDetailViewModel.Companion.MAX_REPORT_COUNT
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailBinding

    private val viewModel: PostDetailViewModel by viewModels {
        InjectorUtils.providePostDetailViewModelFactory(this)
    }

    private val createPostActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.isUpdate = true

            viewModel.refreshPostList()
        }
    }

    private val updateReplyActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val reply = result.data?.getParcelableExtra("reply") ?: ListItem.Reply()

            viewModel.updateReply(reply)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        binding.rvPost.adapter = ReplyListAdapter()

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.run {
            title = if (TextUtils.isEmpty(viewModel.groupName)) getString(R.string.lounge_fragment) else viewModel.groupName

            setDisplayHomeAsUpEnabled(true)
        }
        binding.srlPost.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.srlPost.isRefreshing = false

                viewModel.refreshPostList()
            }, 1000)
        }
        binding.cvBtnSend.setOnClickListener { viewModel.insertReply(binding.etReply.text.toString().trim()) }
        binding.etReply.doOnTextChanged { text, _, _, _ ->
            binding.cvBtnSend.setCardBackgroundColor(ContextCompat.getColor(applicationContext, if (text!!.isNotEmpty()) R.color.colorAccent else R.color.cardview_light_background))
            binding.tvBtnSend.setTextColor(ContextCompat.getColor(applicationContext, if (text.isNotEmpty()) android.R.color.white else android.R.color.darker_gray))
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.replyId >= 0 -> {
                    Toast.makeText(this, getString(R.string.send_complete), Toast.LENGTH_LONG).show()

                    // 전송할때마다 하단으로
                    moveToBottom()
                    binding.etReply.setText("")
                    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(binding.cvBtnSend.windowToken, 0)
                }
                state.isSetResultOK -> {
                    setResult(Activity.RESULT_OK)
                    finish()
                    Toast.makeText(this, if (viewModel.post.reportCount > MAX_REPORT_COUNT) getString(R.string.reported_post) else getString(R.string.delete_complete), Toast.LENGTH_LONG).show()
                }
                state.itemList.isNotEmpty() -> {
                    hideProgressBar()
                    (binding.rvPost.adapter as ReplyListAdapter).submitList(state.itemList)
                    if (viewModel.isBottom)
                        moveToBottom()
                    if (viewModel.isUpdate)
                        deliveryUpdate(viewModel.state.value.itemList[0] as? ListItem.Post ?: viewModel.post)
                }
            }
        }.launchIn(lifecycleScope)
        viewModel.userFlow.onEach { user ->
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
        }.launchIn(lifecycleScope)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {

            // 조건을 위해 xml레이아웃을 사용하지 않고 코드로 옵션메뉴를 구성함
            if (viewModel.isAuth) {
                add(Menu.NONE, 1, Menu.NONE, getString(R.string.edit))
                add(Menu.NONE, 2, Menu.NONE, R.string.delete)
            } else {
                add(Menu.NONE, 3, Menu.NONE, R.string.report)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        1 -> {
            (viewModel.state.value.itemList[0] as? ListItem.Post)?.also { post ->
                val intent = Intent(this, CreatePostActivity::class.java)
                    .putExtra("type", TYPE_UPDATE)
                    .putExtra("post", post)

                createPostActivityResultLauncher.launch(intent)
            }
            true
        }
        2 -> {
            showAlertDialog(getString(R.string.delete_title), getString(R.string.delete_message), viewModel::deletePost)
            true
        }
        3 -> {
            showAlertDialog(getString(R.string.report_title), getString(R.string.report_message), viewModel::togglePostReport)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.groupId) {
        0 -> {
            viewModel.state.value.itemList.let { list ->
                if (list[item.itemId] is ListItem.Post) (list[item.itemId] as ListItem.Post).text else (list[item.itemId] as ListItem.Reply).reply
            }.also { text ->
                (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(null, text))
            }
            Toast.makeText(applicationContext, "클립보드에 복사되었습니다!", Toast.LENGTH_LONG).show()
            true
        }
        1 -> {
            val intent = Intent(this, UpdateReplyActivity::class.java)
                .putExtra("reply", viewModel.state.value.itemList[item.itemId] as? ListItem.Reply)

            updateReplyActivityResultLauncher.launch(intent)
            true
        }
        2 -> {
            viewModel.deleteReply(viewModel.state.value.itemList[item.itemId] as? ListItem.Reply ?: ListItem.Reply())
            true
        }
        else -> super.onContextItemSelected(item)
    }

    private fun moveToBottom() {
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.isBottom = false

            binding.rvPost.scrollToPosition(binding.rvPost.adapter?.itemCount?.minus(1) ?: 0)
        }, 300)
    }

    private fun deliveryUpdate(post: ListItem.Post) {
        val intent = Intent(this, PostFragment::class.java).putExtra("post", post)

        setResult(RESULT_OK, intent)
    }

    private fun showAlertDialog(title: String, message: String, action: () -> Unit) {
        AlertDialog.Builder(this)
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

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }
}