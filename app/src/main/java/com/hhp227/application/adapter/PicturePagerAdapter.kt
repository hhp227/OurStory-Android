package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.databinding.ItemImageFullscreenBinding
import com.hhp227.application.model.ListItem

class PicturePagerAdapter : ListAdapter<ListItem.Image, PicturePagerAdapter.ItemHolder>(PictureDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(ItemImageFullscreenBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemHolder(private val binding: ItemImageFullscreenBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageItem: ListItem.Image) {
            binding.item = imageItem

            binding.executePendingBindings()
        }
    }
}

private class PictureDiffCallback : DiffUtil.ItemCallback<ListItem.Image>() {
    override fun areItemsTheSame(oldItem: ListItem.Image, newItem: ListItem.Image): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ListItem.Image, newItem: ListItem.Image): Boolean {
        return oldItem == newItem
    }
}