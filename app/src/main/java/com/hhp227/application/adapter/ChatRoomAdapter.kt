package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.databinding.ItemChatRoomBinding
import com.hhp227.application.dto.ChatRoomItem

class ChatRoomAdapter : ListAdapter<ChatRoomItem, ChatRoomAdapter.ChatRoomViewHolder>(ChatRoomDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        return ChatRoomViewHolder(ItemChatRoomBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatRoomViewHolder(val binding: ItemChatRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatRoomItem) {
            binding.tvName.text = item.name
            binding.tvTimestamp.text = item.timeStamp
        }
    }
}

private class ChatRoomDiffCallback : DiffUtil.ItemCallback<ChatRoomItem>() {
    override fun areItemsTheSame(oldItem: ChatRoomItem, newItem: ChatRoomItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ChatRoomItem, newItem: ChatRoomItem): Boolean {
        return oldItem == newItem
    }
}