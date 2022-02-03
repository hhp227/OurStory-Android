package com.hhp227.application.adapter

import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.ItemEmptyBinding
import com.hhp227.application.databinding.ItemPostBinding
import com.hhp227.application.databinding.LoadMoreBinding
import com.hhp227.application.dto.PostItem
import com.hhp227.application.util.Utils

class PostListAdapter : ListAdapter<PostItem, RecyclerView.ViewHolder>(ItemDiffCallback()) {
    private lateinit var onItemClickListener: OnItemClickListener

    private var footerVisibility = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        TYPE_POST -> ItemHolder(ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        TYPE_LOADER -> FooterHolder(LoadMoreBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        TYPE_EMPTY -> EmptyHolder(ItemEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> throw RuntimeException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemHolder -> holder.bind(getItem(position) as PostItem.Post)
            is FooterHolder -> holder.bind()
            is EmptyHolder -> holder.bind(getItem(position) as PostItem.Empty)
            else -> Unit
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PostItem.Post -> TYPE_POST
            is PostItem.Empty -> TYPE_EMPTY
            is PostItem.Loader -> TYPE_LOADER
            else -> super.getItemViewType(position)
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun setLoaderVisibility(visibility: Int) {
        footerVisibility = visibility
    }

    fun interface OnItemClickListener {
        fun onItemClick(v: View, pos: Int)
    }

    inner class ItemHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.apply {
                setOnClickListener { v -> onItemClickListener.onItemClick(v, adapterPosition) }
                binding.cardView.setOnClickListener { v -> onItemClickListener.onItemClick(v, adapterPosition) }
                binding.llReply.setOnClickListener { v -> onItemClickListener.onItemClick(v, adapterPosition) }
                binding.llLike.setOnClickListener {
                    val post = currentList[adapterPosition] as PostItem.Post
                    val jsonObjectRequest = object : JsonObjectRequest(Method.GET, URLs.URL_POST_LIKE.replace("{POST_ID}", post.id.toString()), null, Response.Listener { response ->
                        if (!response.getBoolean("error")) {
                            val result = response.getString("result")
                            post.likeCount = if (result == "insert") post.likeCount + 1 else post.likeCount - 1

                            notifyItemChanged(adapterPosition)
                        } else
                            Log.e("", response.getString("message"))
                    }, Response.ErrorListener { error ->
                        VolleyLog.e("", error.message)
                    }) {
                        override fun getHeaders(): MutableMap<String, String?> = hashMapOf("Authorization" to AppController.getInstance().preferenceManager.user.apiKey)
                    }

                    AppController.getInstance().addToRequestQueue(jsonObjectRequest)
                }
            }
        }

        fun bind(post: PostItem.Post) = with(binding) {
            tvName.text = post.name
            tvCreateAt.text = Utils.getPeriodTimeGenerator(root.context, post.timeStamp)
            if (!TextUtils.isEmpty(post.text)) {
                tvText.text = post.text
                tvText.maxLines = CONTENT_MAX_LINE
                tvText.visibility = View.VISIBLE
            } else
                tvText.visibility = View.GONE
            tvTextMore.visibility = if (!TextUtils.isEmpty(post.text) && tvText.lineCount > CONTENT_MAX_LINE) View.VISIBLE else View.GONE
            if (post.imageItemList.isNotEmpty()) {
                ivPost.visibility = View.VISIBLE

                Glide.with(root.context)
                    .load(URLs.URL_POST_IMAGE_PATH + post.imageItemList[0].image)
                    .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                    .transition(DrawableTransitionOptions.withCrossFade(150))
                    .into(ivPost)
            } else
                ivPost.visibility = View.GONE
            tvReplyCount.text = post.replyCount.toString()
            tvLikeCount.text = post.likeCount.toString()
            tvLikeCount.visibility = if (post.likeCount == 0) View.GONE else View.VISIBLE
            ivFavorites.visibility = if (post.likeCount == 0) View.GONE else View.VISIBLE
            llReply.tag = adapterPosition
            llLike.tag = adapterPosition

            Glide.with(root.context)
                .load(URLs.URL_USER_PROFILE_IMAGE + post.profileImage)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(ivProfileImage)
        }
    }

    inner class FooterHolder(val binding: LoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.pbMore.visibility = footerVisibility
            binding.tvListFooter.visibility = footerVisibility
        }
    }

    inner class EmptyHolder(val binding: ItemEmptyBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(emptyItem: PostItem.Empty) {
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

private class ItemDiffCallback : DiffUtil.ItemCallback<PostItem>() {
    override fun areItemsTheSame(oldItem: PostItem, newItem: PostItem): Boolean {
        return if (oldItem is PostItem.Post && newItem is PostItem.Post) {
            oldItem.id == newItem.id
        } else {
            oldItem.hashCode() == newItem.hashCode()
        }
    }

    override fun areContentsTheSame(oldItem: PostItem, newItem: PostItem) = oldItem == newItem
}