package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hhp227.application.R
import com.hhp227.application.util.URLs
import com.hhp227.application.databinding.ListItemMessageLeftBinding
import com.hhp227.application.databinding.ListItemMessageRightBinding
import com.hhp227.application.model.ChatItem
import com.hhp227.application.util.DateUtil

class MessageListAdapter : ListAdapter<ChatItem.Message, MessageListAdapter.MessageViewHolder>(MessageDiffCallback()) {
    var userId: Int = 0

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
        return if (getItem(position).user?.id == userId) TYPE_RIGHT else TYPE_LEFT
    }

    sealed class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class LeftMessageViewHolder(val binding: ListItemMessageLeftBinding) : MessageViewHolder(binding.root) {
            fun bind(item: ChatItem.Message) = with(binding) {
                txtMsg.text = item.message
                lblMsgFrom.text = item.user?.name
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

            fun setProfileImage(item: ChatItem.Message) = with(binding) {
                ivProfileImage.load(URLs.URL_USER_PROFILE_IMAGE + item.user?.profileImage) {
                    placeholder(R.drawable.profile_img_circle)
                    error(R.drawable.profile_img_circle)
                    transformations(CircleCropTransformation())
                }
            }
        }

        class RightMessageViewHolder(val binding: ListItemMessageRightBinding) : MessageViewHolder(binding.root) {
            fun bind(item: ChatItem.Message) = with(binding) {
                txtMsg.text = item.message
                lblMsgFrom.text = item.user?.name
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

            fun setProfileImage(item: ChatItem.Message) = with(binding) {
                profilePic.load(URLs.URL_USER_PROFILE_IMAGE + item.user?.profileImage) {
                    placeholder(R.drawable.profile_img_circle)
                    error(R.drawable.profile_img_circle)
                    transformations(CircleCropTransformation())
                }
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