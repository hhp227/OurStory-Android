package com.hhp227.application.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.ItemReplyBinding
import com.hhp227.application.databinding.PostDetailBinding
import com.hhp227.application.dto.ListItem
import com.hhp227.application.fragment.PostDetailFragmentDirections
import com.hhp227.application.util.DateUtil

class ReplyListAdapter : ListAdapter<ListItem, RecyclerView.ViewHolder>(ReplyDiffCallback()) {
    private lateinit var headerHolder: HeaderHolder

    private lateinit var onItemLongClickListener: OnItemLongClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        TYPE_POST -> HeaderHolder(PostDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { headerHolder = it }
        TYPE_REPLY -> ItemHolder(ItemReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> throw RuntimeException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderHolder) {
            holder.bind(getItem(position) as ListItem.Post)
        } else if (holder is ItemHolder) {
            holder.bind(getItem(position) as ListItem.Reply)
        }
    }

    override fun getItemViewType(position: Int): Int = if (getItem(position) is ListItem.Reply) TYPE_REPLY else TYPE_POST

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        this.onItemLongClickListener = listener
    }

    fun interface OnItemLongClickListener {
        fun onLongClick(v: View, p: Int)
    }

    inner class HeaderHolder(val binding: PostDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnLongClickListener { v ->
                onItemLongClickListener.onLongClick(v, bindingAdapterPosition)
                true
            }
        }

        fun bind(post: ListItem.Post) = with(binding) {
            tvName.text = post.name
            tvCreateAt.text = DateUtil.getPeriodTimeGenerator(root.context, post.timeStamp)

            if (!TextUtils.isEmpty(post.text)) {
                tvText.text = post.text
                tvText.visibility = View.VISIBLE
            } else {
                tvText.visibility = View.GONE
            }
            if (post.imageItemList.isNotEmpty()) {
                llImage.visibility = View.VISIBLE

                llImage.removeAllViews()
                post.imageItemList.forEachIndexed { index, imageItem ->
                    ImageView(root.context).apply {
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.FIT_XY

                        setPadding(0, 0, 0, 30)
                        Glide.with(context)
                            .load("${URLs.URL_POST_IMAGE_PATH}${imageItem.image}")
                            .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                            .into(this)
                        setOnClickListener {
                            val directions = PostDetailFragmentDirections.actionPostDetailFragmentToPictureFragment(post.imageItemList.toTypedArray(), index)

                            findNavController().navigate(directions)
                        }
                    }.also { llImage.addView(it) } // apply().also() -> run()으로 바꿀수 있음
                }
            } else {
                llImage.visibility = View.GONE
            }
            tvLikeCount.text = post.likeCount.toString()
            tvReplyCount.text = post.replyCount.toString()

            Glide.with(root.context)
                .load("${URLs.URL_USER_PROFILE_IMAGE}${post.profileImage}")
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(ivProfileImage)
        }
    }

    inner class ItemHolder(val binding: ItemReplyBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnLongClickListener { v ->
                onItemLongClickListener.onLongClick(v, bindingAdapterPosition)
                true
            }
        }

        fun bind(replyItem: ListItem.Reply) = with(binding) {
            tvName.text = replyItem.name
            tvReply.text = replyItem.reply
            tvCreateAt.text = DateUtil.getPeriodTimeGenerator(root.context, replyItem.timeStamp)

            Glide.with(root.context)
                .load("${URLs.URL_USER_PROFILE_IMAGE}${(replyItem.profileImage)}")
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(ivProfileImage)
        }
    }

    companion object {
        private const val TYPE_POST = 10
        private const val TYPE_REPLY = 20
    }
}

private class ReplyDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        val isSamePost = oldItem is ListItem.Post
                && newItem is ListItem.Post
                && oldItem.text == newItem.text
        val isSameReply = oldItem is ListItem.Reply
                && newItem is ListItem.Reply
                && oldItem.id == newItem.id
        return isSamePost || isSameReply
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem == newItem
    }
}