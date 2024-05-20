package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.paging.CombinedLoadStates
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.databinding.ItemPostBinding
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.User

class PostPagingDataAdapter : PagingDataAdapter<ListItem.Post, RecyclerView.ViewHolder>(PostItemDiffCallback()) {
    private lateinit var onItemClickListener: OnItemClickListener

    val loadState: LiveData<CombinedLoadStates> get() = loadStateFlow.asLiveData()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemHolder).bind(getItem(position) as ListItem.Post)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        payloads.forEach { payload ->
            if (payload is ListItem.Post) {
                getItem(position)?.let {
                    it.likeCount = payload.likeCount

                    (holder as ItemHolder).bind(it)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun updatePost(post: ListItem.Post) {
        snapshot().items.indexOfFirst { item ->
            item.id == post.id
        }.also { position ->
            if (position >= 0) {
                notifyItemChanged(position, post)
            }
        }
    }

    fun updateProfileImages(user: User) {
        snapshot().items
            .forEach { post ->
                if (post.userId == user.id) {
                    post.profileImage = user.profileImage
                }
            }
    }

    interface OnItemClickListener {
        fun onItemClick(v: View, p: Int)

        fun onLikeClick(p: Int)
    }

    inner class ItemHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.apply {
                setOnClickListener { onItemClickListener.onItemClick(it, bindingAdapterPosition) }
                binding.cardView.setOnClickListener { onItemClickListener.onItemClick(it, bindingAdapterPosition) }
                binding.llReply.setOnClickListener { onItemClickListener.onItemClick(it, bindingAdapterPosition) }
                binding.llLike.setOnClickListener { onItemClickListener.onLikeClick(bindingAdapterPosition) }
            }
        }

        fun bind(post: ListItem.Post) = with(binding) {
            item = post

            executePendingBindings()
        }
    }

    companion object {
        private const val TYPE_POST = 0
        private const val TYPE_LOADER = 1
        private const val TYPE_EMPTY = 2
    }
}

private class PostItemDiffCallback : DiffUtil.ItemCallback<ListItem.Post>() {
    override fun areItemsTheSame(oldItem: ListItem.Post, newItem: ListItem.Post): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ListItem.Post, newItem: ListItem.Post): Boolean {
        val isSamePostLike = oldItem.likeCount == newItem.likeCount
        val isSamePostId = oldItem.id == newItem.id
        return isSamePostId || isSamePostLike
    }
}