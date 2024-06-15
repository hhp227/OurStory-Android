import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.databinding.ListItemMessageLeftBinding
import com.hhp227.application.databinding.ListItemMessageRightBinding
import com.hhp227.application.model.ChatItem

class MessageListAdapter(private val userId: Int) : ListAdapter<ChatItem.Message, MessageListAdapter.MessageViewHolder>(MessageDiffCallback()) {
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
                holder.bind(
                    prevMessage = if (position > 0) getItem(position - 1) else null,
                    currentMessage = getItem(position),
                    nextMessage = if (position < currentList.size - 1) getItem(position + 1) else null
                )
                /*if (position > 0 && DateUtil.getTimeStamp(getItem(position - 1).time) == DateUtil.getTimeStamp(getItem(position).time) && getItem(position - 1).user?.id == getItem(position).user?.id) {
                    holder.sameTimeStamp()
                    holder.setProfileImageVisible(View.INVISIBLE)
                } else {
                //holder.setProfileImage(getItem(position)) 지울코드
                }*/
                /*try {
                    // 타임스탬프와 유저넘버가 이후포지션과 같다면
                    if (Utils.getTimeStamp(getItem(position).time) == Utils.getTimeStamp(getItem(position + 1).time) && getItem(position).user.id == getItem(position + 1).user.id) {
                        holder.setTimeStampVisible()
                    }
                } catch (e: Exception) {
                }*/
            }
            is MessageViewHolder.RightMessageViewHolder -> {
                holder.bind(
                    prevMessage = if (position > 0) getItem(position - 1) else null,
                    currentMessage = getItem(position),
                    nextMessage = if (position < currentList.size - 1) getItem(position + 1) else null
                )
                /*if (position > 0 && DateUtil.getTimeStamp(getItem(position - 1).time) == DateUtil.getTimeStamp(getItem(position).time) && getItem(position - 1).user?.id == getItem(position).user?.id) {
                    holder.sameTimeStamp()
                } else {
                //holder.setProfileImage(getItem(position)) 지울코드
                }*/
                /*try {
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
            fun bind(prevMessage: ChatItem.Message?, currentMessage: ChatItem.Message, nextMessage: ChatItem.Message?) = with(binding) {
                prevItem = prevMessage
                item = currentMessage
                nextItem = nextMessage

                Log.e("TEST", "prevMessage: $prevMessage, currentMessage: $currentMessage, nextMessage: $nextMessage")
                //TODO 시간, 패딩, 프로필 이미지 등등 수정할것
                executePendingBindings()
            }

            fun sameTimeStamp() = with(binding) {
                lblMsgFrom.visibility = View.GONE

                messageBox.setPadding(messageBox.paddingLeft, 0, messageBox.paddingRight, messageBox.paddingBottom)
            }

            fun setProfileImageVisible(visible: Int) {
                binding.ivProfileImage.visibility = visible
            }

            fun setTimeStampVisible() = with(binding) {
                msgTime.visibility = View.INVISIBLE
            }
        }

        class RightMessageViewHolder(val binding: ListItemMessageRightBinding) : MessageViewHolder(binding.root) {
            fun bind(prevMessage: ChatItem.Message?, currentMessage: ChatItem.Message, nextMessage: ChatItem.Message?) = with(binding) {
                prevItem = prevMessage
                item = currentMessage
                nextItem = nextMessage

                Log.e("TEST", "prevMessage: $prevMessage, currentMessage: $currentMessage, nextMessage: $nextMessage")
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

    init {
        Log.e("TEST", "MessageListAdapter $userId")
    }

    companion object {
        private const val TYPE_LEFT = 0
        private const val TYPE_RIGHT = 1
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<ChatItem.Message>() {
    override fun areItemsTheSame(oldItem: ChatItem.Message, newItem: ChatItem.Message): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ChatItem.Message, newItem: ChatItem.Message): Boolean {
        return oldItem.id == newItem.id
    }
}