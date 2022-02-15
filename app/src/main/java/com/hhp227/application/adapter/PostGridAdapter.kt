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
import com.hhp227.application.dto.ListItem

class PostGridAdapter : ListAdapter<ListItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder) {
            holder.bind(getItem(position) as ListItem.Post)
        }
    }

    inner class ItemHolder(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListItem.Post) {
            Glide.with(binding.root.context)
                .load(URLs.URL_POST_IMAGE_PATH + item.imageItemList.first().image)
                .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .into(binding.ivImage)
        }
    }
}

private class PostDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return if (oldItem is ListItem.Post && newItem is ListItem.Post) {
            oldItem.id == newItem.id
        } else {
            oldItem.hashCode() == newItem.hashCode()
        }
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem) = oldItem == newItem
}