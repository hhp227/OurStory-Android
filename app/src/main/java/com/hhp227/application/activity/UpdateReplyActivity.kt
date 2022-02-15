package com.hhp227.application.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.data.ReplyRepository
import com.hhp227.application.databinding.ActivityReplyModifyBinding
import com.hhp227.application.databinding.InputTextBinding
import com.hhp227.application.dto.ListItem
import com.hhp227.application.viewmodel.UpdateReplyViewModel
import com.hhp227.application.viewmodel.UpdateReplyViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class UpdateReplyActivity : AppCompatActivity() {
    private val viewModel: UpdateReplyViewModel by viewModels {
        UpdateReplyViewModelFactory(ReplyRepository(), this, intent.extras)
    }

    private lateinit var binding: ActivityReplyModifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReplyModifyBinding.inflate(layoutInflater)
        binding.recyclerView.adapter = object : RecyclerView.Adapter<ItemHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder = ItemHolder(InputTextBinding.inflate(layoutInflater))

            override fun getItemCount(): Int = 1

            override fun onBindViewHolder(holder: ItemHolder, position: Int) {
                holder.bind(viewModel.reply)
            }
        }

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> {

                }
                state.text != null -> {
                    val reply = viewModel.reply.apply {
                        reply = state.text
                    }
                    val intent = Intent(this, PostDetailActivity::class.java).putExtra("reply", reply)

                    setResult(RESULT_OK, intent)
                    finish()
                    currentFocus?.let {
                        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(it.windowToken, 0)
                    }
                }
                state.error.isNotBlank() -> {
                    Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.write, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        R.id.actionSend -> {
            val text = (binding.recyclerView.getChildViewHolder(binding.recyclerView.getChildAt(0)) as ItemHolder).binding.etText.text.toString()

            viewModel.updateReply(text)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    inner class ItemHolder(val binding: InputTextBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(replyItem: ListItem.Reply) {
            binding.etText.setText(replyItem.reply)
        }
    }
}