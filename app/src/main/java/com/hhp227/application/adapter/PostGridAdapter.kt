package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.ItemAlbumBinding
import com.hhp227.application.dto.PostItem

class PostGridAdapter : ListAdapter<PostItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder) {
            holder.bind(getItem(position) as PostItem.Post)
        }
    }

    inner class ItemHolder(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PostItem.Post) {
            Glide.with(binding.root.context)
                .load(URLs.URL_POST_IMAGE_PATH + item.imageItemList.first().image)
                .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .into(binding.ivImage)
        }
    }
}

private class PostDiffCallback : DiffUtil.ItemCallback<PostItem>() {
    override fun areItemsTheSame(oldItem: PostItem, newItem: PostItem): Boolean {
        return if (oldItem is PostItem.Post && newItem is PostItem.Post) {
            oldItem.id == newItem.id
        } else {
            oldItem.hashCode() == newItem.hashCode()
        }
    }

    override fun areContentsTheSame(oldItem: PostItem, newItem: PostItem) = oldItem == newItem
}