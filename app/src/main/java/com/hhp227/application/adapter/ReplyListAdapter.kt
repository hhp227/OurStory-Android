package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.databinding.ItemReplyBinding
import com.hhp227.application.databinding.PostDetailBinding
import com.hhp227.application.model.ListItem

class ReplyListAdapter : ListAdapter<ListItem, RecyclerView.ViewHolder>(ReplyDiffCallback()) {
    private lateinit var onImageClickListener: OnImageClickListener

    var userId: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        TYPE_POST -> HeaderHolder(PostDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        TYPE_REPLY -> ItemHolder(ItemReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> throw RuntimeException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderHolder) {
            holder.bind(getItem(position) as ListItem.Post)
        } else if (holder is ItemHolder) {
            holder.bind(getItem(position) as ListItem.Reply)
        }
    }

    override fun getItemViewType(position: Int): Int = if (getItem(position) is ListItem.Reply) TYPE_REPLY else TYPE_POST

    fun setOnImageClickListener(listener: OnImageClickListener) {
        this.onImageClickListener = listener
    }

    fun interface OnImageClickListener {
        fun onImageClick(list: List<ListItem.Image>, i: Int)
    }

    inner class HeaderHolder(val binding: PostDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: ListItem.Post) = with(binding) {
            item = post

            executePendingBindings()
        }

        init {
            binding.onImageClickListener = onImageClickListener

            binding.root.setOnCreateContextMenuListener { menu, v, menuInfo ->
                menu?.setHeaderTitle(v?.context?.getString(R.string.select_action))
                menu?.add(bindingAdapterPosition, 1000, Menu.NONE, v?.context?.getString(R.string.copy_content))
            }
            binding.root.setOnLongClickListener { v ->
                v.showContextMenu()
                true
            }
        }
    }

    inner class ItemHolder(val binding: ItemReplyBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reply: ListItem.Reply) = with(binding) {
            item = reply

            executePendingBindings()
        }

        init {
            binding.root.setOnCreateContextMenuListener { menu, v, menuInfo ->
                menu.setHeaderTitle(v.context.getString(R.string.select_action))
                menu.add(bindingAdapterPosition, 1000, Menu.NONE, v.context.getString(R.string.copy_content))
                if ((currentList[bindingAdapterPosition] as ListItem.Reply).userId == userId) {
                    menu.add(bindingAdapterPosition, 1001, Menu.NONE, v.context.getString(R.string.edit_comment))
                    menu.add(bindingAdapterPosition, 1002, Menu.NONE, v.context.getString(R.string.delete_comment))
                }
            }
            binding.root.setOnLongClickListener { v ->
                v.showContextMenu()
                true
            }
        }
    }

    companion object {
        private const val TYPE_POST = 10
        private const val TYPE_REPLY = 20
    }
}

private class ReplyDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        val isSamePost = oldItem is ListItem.Post
                && newItem is ListItem.Post
                && oldItem.text == newItem.text
        val isSameReply = oldItem is ListItem.Reply
                && newItem is ListItem.Reply
                && oldItem.id == newItem.id
        return isSamePost || isSameReply
    }
}