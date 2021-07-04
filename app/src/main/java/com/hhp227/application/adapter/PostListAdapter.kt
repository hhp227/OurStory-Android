package com.hhp227.application.adapter

import android.annotation.SuppressLint
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
import com.hhp227.application.databinding.ItemPostBinding
import com.hhp227.application.databinding.LoadMoreBinding
import com.hhp227.application.dto.PostItem
import com.hhp227.application.util.Utils

// TODO Tab1Fragment에도 이 어댑터 달기
class PostListAdapter : ListAdapter<Any, RecyclerView.ViewHolder>(ItemDiffCallback()) {
    private lateinit var onItemClickListener: OnItemClickListener

    private var footerVisibility = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        TYPE_POST -> ItemHolder(ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        TYPE_LOADER -> FooterHolder(LoadMoreBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> throw RuntimeException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemHolder -> holder.bind(getItem(position) as PostItem)
            is FooterHolder -> holder.bind()
            else -> Unit
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun addFooterView(footer: Any?) {
        currentList.add(footer)
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
                    val postItem = currentList[adapterPosition] as PostItem
                    val jsonObjectRequest = object : JsonObjectRequest(Method.GET, URLs.URL_POST_LIKE.replace("{POST_ID}", postItem.id.toString()), null, Response.Listener { response ->
                        if (!response.getBoolean("error")) {
                            val result = response.getString("result")
                            postItem.likeCount = if (result == "insert") postItem.likeCount + 1 else postItem.likeCount - 1

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

        fun bind(postItem: PostItem) = with(binding) {
            tvName.text = postItem.name
            tvCreateAt.text = Utils.getPeriodTimeGenerator(root.context, postItem.timeStamp)
            if (!TextUtils.isEmpty(postItem.text)) {
                tvText.text = postItem.text
                tvText.maxLines = CONTENT_MAX_LINE
                tvText.visibility = View.VISIBLE
            } else
                tvText.visibility = View.GONE
            tvTextMore.visibility = if (!TextUtils.isEmpty(postItem.text) && tvText.lineCount > CONTENT_MAX_LINE) View.VISIBLE else View.GONE
            if (postItem.imageItemList.isNotEmpty()) {
                ivPost.visibility = View.VISIBLE

                Glide.with(root.context)
                    .load(URLs.URL_POST_IMAGE_PATH + postItem.imageItemList[0].image)
                    .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                    .transition(DrawableTransitionOptions.withCrossFade(150))
                    .into(ivPost)
            } else
                ivPost.visibility = View.GONE
            tvReplyCount.text = postItem.replyCount.toString()
            tvLikeCount.text = postItem.likeCount.toString()
            tvLikeCount.visibility = if (postItem.likeCount == 0) View.GONE else View.VISIBLE
            ivFavorites.visibility = if (postItem.likeCount == 0) View.GONE else View.VISIBLE
            llReply.tag = adapterPosition
            llLike.tag = adapterPosition

            Glide.with(root.context)
                .load(URLs.URL_USER_PROFILE_IMAGE + postItem.profileImage)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(ivProfileImage)
        }
    }

    inner class FooterHolder(val binding: LoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {

        }
    }

    companion object {
        private const val TYPE_POST = 0
        private const val TYPE_LOADER = 1
        private const val CONTENT_MAX_LINE = 4
    }
}

private class ItemDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return if (oldItem is PostItem && newItem is PostItem) {
            oldItem.id == newItem.id
        } else {
            oldItem.hashCode() == newItem.hashCode()
        }
    }
}