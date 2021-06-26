package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hhp227.application.R
import com.hhp227.application.dto.GalleryItem
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_grid_image.view.*
import kotlin.properties.Delegates

class ImageSelectAdapter : ListAdapter<GalleryItem, ImageSelectAdapter.ImageViewHolder>(ImageDiffCallback()) {
    var currentPosition by Delegates.observable(-1) { _, oldValue, newValue ->
        if (newValue in currentList.indices) {
            notifyItemChanged(oldValue)
            notifyItemChanged(newValue)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_grid_image, parent, false))
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ImageViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        init {
            containerView.setOnClickListener {
                currentPosition = adapterPosition
                currentList[adapterPosition].isSelected = !currentList[adapterPosition].isSelected
            }
        }

        fun bind(item: GalleryItem) {
            containerView.tv_select.text = item.isSelected.toString()

            Glide.with(itemView.context)
                .load(item.uri)
                .thumbnail(0.33f)
                .centerCrop()
                .into(containerView.iv_image)
            containerView.cl_image.setBackgroundResource(if (item.isSelected) R.drawable.ic_popup_active else 0)
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