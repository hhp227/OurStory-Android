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
import com.hhp227.application.databinding.ItemEmptyBinding
import com.hhp227.application.databinding.ItemPostBinding
import com.hhp227.application.databinding.LoadMoreBinding
import com.hhp227.application.dto.ListItem

class PostGridAdapter : ListAdapter<ListItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    private var footerVisibility = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_POST -> ItemHolder(ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            TYPE_LOADER -> FooterHolder(LoadMoreBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            TYPE_EMPTY -> EmptyHolder(ItemEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw RuntimeException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder) {
            holder.bind(getItem(position) as ListItem.Post)
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

    inner class ItemHolder(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListItem.Post) {
            Glide.with(binding.root.context)
                .load(URLs.URL_POST_IMAGE_PATH + item.attachment.imageItemList.first().image)
                .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .into(binding.ivImage)
        }
    }

    inner class FooterHolder(val binding: LoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {
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

class PostDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return if (oldItem is ListItem.Post && newItem is ListItem.Post) {
            oldItem.id == newItem.id
        } else {
            oldItem.hashCode() == newItem.hashCode()
        }
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem) = oldItem == newItem
}