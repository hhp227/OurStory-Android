package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.databinding.ItemReplyBinding
import com.hhp227.application.databinding.PostDetailBinding
import com.hhp227.application.model.ListItem

class ReplyListAdapter : ListAdapter<ListItem, RecyclerView.ViewHolder>(ReplyDiffCallback()) {
    private lateinit var onItemLongClickListener: OnItemLongClickListener

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

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        this.onItemLongClickListener = listener
    }

    fun interface OnItemLongClickListener {
        fun onLongClick(v: View, p: Int)
    }

    inner class HeaderHolder(val binding: PostDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnLongClickListener { v ->
                onItemLongClickListener.onLongClick(v, bindingAdapterPosition)
                true
            }
        }

        fun bind(post: ListItem.Post) = with(binding) {
            item = post

            executePendingBindings()
        }
    }

    inner class ItemHolder(val binding: ItemReplyBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnLongClickListener { v ->
                onItemLongClickListener.onLongClick(v, bindingAdapterPosition)
                true
            }
        }

        fun bind(replyItem: ListItem.Reply) = with(binding) {
            item = replyItem

            executePendingBindings()
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