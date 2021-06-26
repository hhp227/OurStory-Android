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
import com.hhp227.application.dto.PostItem
import com.hhp227.application.app.URLs
import com.hhp227.application.util.Utils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_post.view.*

// TODO Tab1Fragment에도 이 어댑터 달기
class PostListAdapter : ListAdapter<Any, RecyclerView.ViewHolder>(ItemDiffCallback()) {
    private lateinit var onItemClickListener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        TYPE_POST -> ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false))
        TYPE_LOADER -> FooterHolder(LayoutInflater.from(parent.context).inflate(R.layout.load_more, parent, false))
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

    fun interface OnItemClickListener {
        fun onItemClick(v: View, pos: Int)
    }

    inner class ItemHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        init {
            containerView.apply {
                setOnClickListener { v -> onItemClickListener.onItemClick(v, adapterPosition) }
                cardView.setOnClickListener { v -> onItemClickListener.onItemClick(v, adapterPosition) }
                llReply.setOnClickListener { v -> onItemClickListener.onItemClick(v, adapterPosition) }
                llLike.setOnClickListener {
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

        fun bind(postItem: PostItem) = with(containerView) {
            tvName.text = postItem.name
            tvCreateAt.text = Utils.getPeriodTimeGenerator(context, postItem.timeStamp)
            if (!TextUtils.isEmpty(postItem.text)) {
                tvText.text = postItem.text
                tvText.maxLines = CONTENT_MAX_LINE
                tvText.visibility = View.VISIBLE
            } else
                tvText.visibility = View.GONE
            tvTextMore.visibility = if (!TextUtils.isEmpty(postItem.text) && tvText.lineCount > CONTENT_MAX_LINE) View.VISIBLE else View.GONE
            if (postItem.imageItemList.isNotEmpty()) {
                ivPost.visibility = View.VISIBLE

                Glide.with(context)
                    .load(URLs.URL_POST_IMAGE_PATH + postItem.imageItemList[0].image)
                    .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                    .transition(DrawableTransitionOptions.withCrossFade(150))
                    .into(ivPost)
            } else
                ivPost.visibility = View.GONE
            replyCount.text = postItem.replyCount.toString()
            likeCount.text = postItem.likeCount.toString()
            likeCount.visibility = if (postItem.likeCount == 0) View.GONE else View.VISIBLE
            ivFavorites.visibility = if (postItem.likeCount == 0) View.GONE else View.VISIBLE
            llReply.tag = adapterPosition
            llLike.tag = adapterPosition

            Glide.with(context)
                .load(URLs.URL_USER_PROFILE_IMAGE + postItem.profileImage)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(ivProfileImage)
        }
    }

    inner class FooterHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
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

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return if (oldItem is PostItem && newItem is PostItem) {
            oldItem.id == newItem.id
        } else {
            oldItem.hashCode() == newItem.hashCode()
        }
    }
}