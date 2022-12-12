package com.hhp227.application.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hhp227.application.R
import com.hhp227.application.util.URLs
import com.hhp227.application.databinding.ItemPostBinding
import com.hhp227.application.dto.ListItem
import com.hhp227.application.util.DateUtil
import com.hhp227.application.viewmodel.PostDetailViewModel

class PostPagingDataAdapter : PagingDataAdapter<ListItem.Post, RecyclerView.ViewHolder>(PostItemDiffCallback()) {
    private lateinit var onItemClickListener: OnItemClickListener

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemHolder).bind(getItem(position) as ListItem.Post)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
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
            if (post.reportCount > PostDetailViewModel.MAX_REPORT_COUNT) {
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
                        placeholder(R.drawable.ic_launcher)
                        error(R.drawable.ic_launcher)
                        crossfade(150)
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
                    placeholder(R.drawable.profile_img_circle)
                    error(R.drawable.profile_img_circle)
                    transformations(CircleCropTransformation())
                }
            }
        }
    }

    companion object {
        private const val TYPE_POST = 0
        private const val TYPE_LOADER = 1
        private const val TYPE_EMPTY = 2
        private const val CONTENT_MAX_LINE = 4
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