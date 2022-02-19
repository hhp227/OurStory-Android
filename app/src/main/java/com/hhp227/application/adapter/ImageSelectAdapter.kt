package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hhp227.application.R
import com.hhp227.application.databinding.ItemGridImageBinding
import com.hhp227.application.dto.GalleryItem
import kotlin.properties.Delegates

class ImageSelectAdapter : PagingDataAdapter<GalleryItem, ImageSelectAdapter.ImageViewHolder>(ImageDiffCallback()) {
    private lateinit var onItemClickListener: (View, Int) -> Unit

    var currentPosition by Delegates.observable(-1) { _, oldValue, newValue ->
        if (newValue in snapshot().indices) {
            notifyItemChanged(oldValue)
            notifyItemChanged(newValue)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(ItemGridImageBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnItemClickListener(listener: (View, Int) -> Unit) {
        onItemClickListener = listener
    }

    inner class ImageViewHolder(val binding: ItemGridImageBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { v ->
                onItemClickListener(v, absoluteAdapterPosition)
            }
        }

        fun bind(item: GalleryItem?) {
            Glide.with(itemView.context)
                .load(item?.uri)
                .thumbnail(0.33f)
                .centerCrop()
                .into(binding.ivImage)
            binding.clImage.setBackgroundResource(if (item?.isSelected == true) R.drawable.ic_popup_active else 0)
        }
    }
}

private class ImageDiffCallback : DiffUtil.ItemCallback<GalleryItem>() {
    override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
        return oldItem.uri == newItem.uri
    }

    override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
        return oldItem == newItem
    }
}