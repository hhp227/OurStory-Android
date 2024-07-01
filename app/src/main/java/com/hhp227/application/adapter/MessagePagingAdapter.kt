package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.databinding.ListItemMessageLeftBinding
import com.hhp227.application.databinding.ListItemMessageRightBinding
import com.hhp227.application.model.ChatItem
import com.hhp227.application.util.DateUtil

class MessagePagingAdapter(private val userId: Int) : PagingDataAdapter<ChatItem.Message, MessagePagingAdapter.MessageViewHolder>(MessageDiffCallback()) {
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        when (holder) {
            is MessageViewHolder.LeftMessageViewHolder -> {
                holder.bind(
                    prevMessage = if (position > 0) getItem(position - 1) else null,
                    currentMessage = getItem(position),
                    nextMessage = if (position < snapshot().size - 1) getItem(position + 1) else null
                )
            }
            is MessageViewHolder.RightMessageViewHolder -> {
                holder.bind(
                    prevMessage = if (position > 0) getItem(position - 1) else null,
                    currentMessage = getItem(position),
                    nextMessage = if (position < snapshot().size - 1) getItem(position + 1) else null
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return when (viewType) {
            TYPE_LEFT -> MessageViewHolder.LeftMessageViewHolder(
                ListItemMessageLeftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            TYPE_RIGHT -> MessageViewHolder.RightMessageViewHolder(
                ListItemMessageRightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> throw NoSuchElementException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.user?.id == userId) TYPE_RIGHT else TYPE_LEFT
    }

    sealed class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class LeftMessageViewHolder(val binding: ListItemMessageLeftBinding) : MessageViewHolder(binding.root) {
            fun bind(prevMessage: ChatItem.Message?, currentMessage: ChatItem.Message?, nextMessage: ChatItem.Message?) = with(binding) {
                item = currentMessage
                isSameTimestampAsPrevItem = prevMessage != null &&
                        DateUtil.getTimeStamp(prevMessage.time) == DateUtil.getTimeStamp(currentMessage?.time)
                isSameUserIdAsPrevItem = prevMessage != null && prevMessage.user?.id == currentMessage?.user?.id
                isSameTimestampAsNextItem = nextMessage != null &&
                        DateUtil.getTimeStamp(currentMessage?.time) == DateUtil.getTimeStamp(nextMessage.time)
                isSameUserIdAsNextItem = nextMessage != null && currentMessage?.user?.id == nextMessage.user?.id

                executePendingBindings()
            }
        }

        class RightMessageViewHolder(val binding: ListItemMessageRightBinding) : MessageViewHolder(binding.root) {
            fun bind(prevMessage: ChatItem.Message?, currentMessage: ChatItem.Message?, nextMessage: ChatItem.Message?) = with(binding) {
                item = currentMessage
                isSameTimestampAsPrevItem = prevMessage != null &&
                        DateUtil.getTimeStamp(prevMessage.time) == DateUtil.getTimeStamp(currentMessage?.time)
                isSameUserIdAsPrevItem = prevMessage != null && prevMessage.user?.id == currentMessage?.user?.id
                isSameTimestampAsNextItem = nextMessage != null &&
                        DateUtil.getTimeStamp(currentMessage?.time) == DateUtil.getTimeStamp(nextMessage.time)
                isSameUserIdAsNextItem = nextMessage != null && currentMessage?.user?.id == nextMessage.user?.id

                executePendingBindings()
            }
        }
    }

    companion object {
        private const val TYPE_LEFT = 0
        private const val TYPE_RIGHT = 1
    }
}

private class MessageDiffCallback : DiffUtil.ItemCallback<ChatItem.Message>() {
    override fun areItemsTheSame(oldItem: ChatItem.Message, newItem: ChatItem.Message): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ChatItem.Message, newItem: ChatItem.Message): Boolean {
        return oldItem.id == newItem.id
    }
}