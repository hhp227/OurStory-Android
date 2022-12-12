package com.hhp227.application.adapter

import android.text.TextUtils
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
import com.hhp227.application.databinding.ItemEmptyBinding
import com.hhp227.application.databinding.ItemPostBinding
import com.hhp227.application.databinding.ItemLoadStateBinding
import com.hhp227.application.dto.ListItem
import com.hhp227.application.util.DateUtil
import com.hhp227.application.viewmodel.PostDetailViewModel.Companion.MAX_REPORT_COUNT

class PostListAdapter : ListAdapter<ListItem, RecyclerView.ViewHolder>(ItemDiffCallback()) {
    private lateinit var onItemClickListener: OnItemClickListener

    private var footerVisibility = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        TYPE_POST -> ItemHolder(ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        TYPE_LOADER -> FooterHolder(ItemLoadStateBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        TYPE_EMPTY -> EmptyHolder(ItemEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> throw RuntimeException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemHolder -> holder.bind(getItem(position) as ListItem.Post)
            is FooterHolder -> holder.bind()
            is EmptyHolder -> holder.bind(getItem(position) as ListItem.Empty)
            else -> Unit
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListItem.Post -> TYPE_POST
            is ListItem.Empty -> TYPE_EMPTY
            is ListItem.Loader -> TYPE_LOADER
            else -> super.getItemViewType(position)
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun setLoaderVisibility(visibility: Int) {
        footerVisibility = visibility
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
            if (post.reportCount > MAX_REPORT_COUNT) {
                llContainer.visibility = View.GONE
                llReported.visibility = View.VISIBLE
            } else {
                llContainer.visibility = View.VISIBLE
                llReported.visibility = View.GONE
                tvName.text = post.name
                tvCreateAt.text = DateUtil.getPeriodTimeGenerator(root.context, post.timeStamp)
                if (!TextUtils.isEmpty(post.text)) {
                    tvText.text = post.text
                    tvText.maxLines = CONTENT_MAX_LINE
                    tvText.visibility = View.VISIBLE
                } else {
                    tvText.visibility = View.GONE
                }
                tvTextMore.visibility = if (!TextUtils.isEmpty(post.text) && tvText.lineCount > CONTENT_MAX_LINE) View.VISIBLE else View.GONE
                if (post.attachment.imageItemList.isNotEmpty()) {
                    ivPost.visibility = View.VISIBLE

                    ivPost.load(URLs.URL_POST_IMAGE_PATH + post.attachment.imageItemList[0].image) {
                        crossfade(150)
                        placeholder(R.drawable.ic_launcher)
                        error(R.drawable.ic_launcher)
                    }
                } else {
                    ivPost.visibility = View.GONE
                }
                tvReplyCount.text = post.replyCount.toString()
                tvLikeCount.text = post.likeCount.toString()
                tvLikeCount.visibility = if (post.likeCount == 0) View.GONE else View.VISIBLE
                ivFavorites.visibility = if (post.likeCount == 0) View.GONE else View.VISIBLE
                llReply.tag = bindingAdapterPosition
                llLike.tag = bindingAdapterPosition

                ivProfileImage.load(URLs.URL_USER_PROFILE_IMAGE + post.profileImage) {
                    crossfade(true)
                    placeholder(R.drawable.profile_img_circle)
                    error(R.drawable.profile_img_circle)
                    transformations(CircleCropTransformation())
                }
            }
        }
    }

    inner class FooterHolder(val binding: ItemLoadStateBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.pbMore.visibility = footerVisibility
            binding.tvListFooter.visibility = footerVisibility
        }
    }

    inner class EmptyHolder(val binding: ItemEmptyBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(emptyItem: ListItem.Empty) {
            binding.tvAdd.text = emptyItem.text

            binding.ivAdd.setImageResource(emptyItem.res)
        }
    }

    companion object {
        private const val TYPE_POST = 0
        private const val TYPE_LOADER = 1
        private const val TYPE_EMPTY = 2
        private const val CONTENT_MAX_LINE = 4
    }
}

private class ItemDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem) = oldItem == newItem

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        val isSamePostLike = oldItem is ListItem.Post
                && newItem is ListItem.Post
                && oldItem.likeCount == newItem.likeCount
        val isSamePostId = oldItem is ListItem.Post
                && newItem is ListItem.Post
                && oldItem.id == newItem.id
        return isSamePostId || isSamePostLike
    }
}