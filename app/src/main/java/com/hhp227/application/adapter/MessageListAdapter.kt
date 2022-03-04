package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.ListItemMessageLeftBinding
import com.hhp227.application.databinding.ListItemMessageRightBinding
import com.hhp227.application.dto.MessageItem
import com.hhp227.application.util.DateUtil

class MessageListAdapter(val userId: Int) : ListAdapter<MessageItem, MessageListAdapter.MessageViewHolder>(MessageDiffCallback()) {
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

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        when (holder) {
            is MessageViewHolder.LeftMessageViewHolder -> {
                holder.bind(getItem(position))
                /*if (position > 0 && Utils.getTimeStamp(getItem(position - 1).time) == Utils.getTimeStamp(getItem(position).time) && getItem(position - 1).user.id == getItem(position).user.id) {
                    holder.sameTimeStamp()
                } else {*/
                    holder.setProfileImage(getItem(position))
                /*}
                try {
                    // 타임스탬프와 유저넘버가 이후포지션과 같다면
                    if (Utils.getTimeStamp(getItem(position).time) == Utils.getTimeStamp(getItem(position + 1).time) && getItem(position).user.id == getItem(position + 1).user.id) {
                        holder.setTimeStampVisible()
                    }
                } catch (e: Exception) {
                }*/
            }
            is MessageViewHolder.RightMessageViewHolder -> {
                holder.bind(getItem(position))
                /*if (position > 0 && Utils.getTimeStamp(getItem(position - 1).time) == Utils.getTimeStamp(getItem(position).time) && getItem(position - 1).user.id == getItem(position).user.id) {
                    holder.sameTimeStamp()
                } else {*/
                    holder.setProfileImage(getItem(position))
                /*}
                try {
                    // 타임스탬프와 유저넘버가 이후포지션과 같다면
                    if (Utils.getTimeStamp(getItem(position).time) == Utils.getTimeStamp(getItem(position + 1).time) && getItem(position).user.id == getItem(position + 1).user.id) {
                        holder.setTimeStampVisible()
                    }
                } catch (e: Exception) {
                }*/
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).user.id == userId) TYPE_RIGHT else TYPE_LEFT
    }

    sealed class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class LeftMessageViewHolder(val binding: ListItemMessageLeftBinding) : MessageViewHolder(binding.root) {
            fun bind(item: MessageItem) = with(binding) {
                txtMsg.text = item.message
                lblMsgFrom.text = item.user.name
                msgTime.text = DateUtil.getTimeStamp(item.time)

                //TODO 시간, 패딩, 프로필 이미지 등등 수정할것
            }

            fun sameTimeStamp() = with(binding) {
                lblMsgFrom.visibility = View.GONE

                messageBox.setPadding(messageBox.paddingLeft, 0, messageBox.paddingRight, messageBox.paddingBottom)
            }

            fun setTimeStampVisible() = with(binding) {
                msgTime.visibility = View.INVISIBLE
            }

            fun setProfileImage(item: MessageItem) = with(binding) {
                Glide.with(ivProfileImage.context)
                    .load(URLs.URL_USER_PROFILE_IMAGE + item.user.profileImage)
                    .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                    .into(ivProfileImage)
            }
        }

        class RightMessageViewHolder(val binding: ListItemMessageRightBinding) : MessageViewHolder(binding.root) {
            fun bind(item: MessageItem) = with(binding) {
                txtMsg.text = item.message
                lblMsgFrom.text = item.user.name
                msgTime.text = DateUtil.getTimeStamp(item.time)

                //TODO 시간, 패딩, 프로필 이미지 등등 수정할것
            }

            fun sameTimeStamp() = with(binding) {
                lblMsgFrom.visibility = View.GONE

                messageBox.setPadding(messageBox.paddingLeft, 0, messageBox.paddingRight, messageBox.paddingBottom)
            }

            fun setTimeStampVisible() = with(binding) {
                msgTime.visibility = View.INVISIBLE
            }

            fun setProfileImage(item: MessageItem) = with(binding) {
                Glide.with(profilePic.context)
                    .load(URLs.URL_USER_PROFILE_IMAGE + item.user.profileImage)
                    .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                    .into(profilePic)
            }
        }
    }

    companion object {
        private const val TYPE_LEFT = 0
        private const val TYPE_RIGHT = 1
    }
}

private class MessageDiffCallback : DiffUtil.ItemCallback<MessageItem>() {
    override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
        return oldItem.id == newItem.id
    }
}