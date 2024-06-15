package com.hhp227.application.adapter

import MessageDiffCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.databinding.ListItemMessageLeftBinding
import com.hhp227.application.databinding.ListItemMessageRightBinding
import com.hhp227.application.model.ChatItem

// MODIFIED
class MessagePagingAdapter(private val userId: Int) : PagingDataAdapter<ChatItem.Message, MessagePagingAdapter.MessageViewHolder>(MessageDiffCallback()) {
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        when (holder) {
            is MessageViewHolder.LeftMessageViewHolder -> {
                holder.bind(getItem(position))
                //holder.setProfileImage(getItem(position))
            }
            is MessageViewHolder.RightMessageViewHolder -> {
                holder.bind(getItem(position))
                //holder.setProfileImage(getItem(position))
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
            fun bind(message: ChatItem.Message?) = with(binding) {
                item = message

                //TODO 시간, 패딩, 프로필 이미지 등등 수정할것
                executePendingBindings()
            }

            fun sameTimeStamp() = with(binding) {
                lblMsgFrom.visibility = View.GONE

                messageBox.setPadding(messageBox.paddingLeft, 0, messageBox.paddingRight, messageBox.paddingBottom)
            }

            fun setTimeStampVisible() = with(binding) {
                msgTime.visibility = View.INVISIBLE
            }
        }

        class RightMessageViewHolder(val binding: ListItemMessageRightBinding) : MessageViewHolder(binding.root) {
            fun bind(message: ChatItem.Message?) = with(binding) {
                item = message

                //TODO 시간, 패딩, 프로필 이미지 등등 수정할것
                executePendingBindings()
            }

            fun sameTimeStamp() = with(binding) {
                lblMsgFrom.visibility = View.GONE

                messageBox.setPadding(messageBox.paddingLeft, 0, messageBox.paddingRight, messageBox.paddingBottom)
            }

            fun setTimeStampVisible() = with(binding) {
                msgTime.visibility = View.INVISIBLE
            }
        }
    }

    companion object {
        private const val TYPE_LEFT = 0
        private const val TYPE_RIGHT = 1
    }
}