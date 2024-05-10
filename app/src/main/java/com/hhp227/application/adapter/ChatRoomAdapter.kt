package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.databinding.ItemChatRoomBinding
import com.hhp227.application.model.ChatItem

class ChatRoomAdapter : ListAdapter<ChatItem.ChatRoom, ChatRoomAdapter.ChatRoomViewHolder>(ChatRoomDiffCallback()) {
    private lateinit var onItemClickListener: (View, Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        return ChatRoomViewHolder(ItemChatRoomBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnItemClickListener(listener: (View, Int) -> Unit) {
        onItemClickListener = listener
    }

    inner class ChatRoomViewHolder(val binding: ItemChatRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { v -> onItemClickListener.invoke(v, bindingAdapterPosition) }
        }

        fun bind(chatRoom: ChatItem.ChatRoom) {
            binding.item = chatRoom

            binding.executePendingBindings()
        }
    }
}

private class ChatRoomDiffCallback : DiffUtil.ItemCallback<ChatItem.ChatRoom>() {
    override fun areItemsTheSame(oldItem: ChatItem.ChatRoom, newItem: ChatItem.ChatRoom): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ChatItem.ChatRoom, newItem: ChatItem.ChatRoom): Boolean {
        return oldItem == newItem
    }
}