package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.ItemGridAdBinding
import com.hhp227.application.databinding.ItemGridHeaderBinding
import com.hhp227.application.databinding.ItemGroupGridBinding
import com.hhp227.application.dto.GroupItem

class GroupGridAdapter : ListAdapter<GroupItem, RecyclerView.ViewHolder>(GroupGridDiffCallback()) {
    private lateinit var onItemClickListener: (View, Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        TYPE_TEXT -> HeaderHolder(ItemGridHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        TYPE_GROUP -> ItemHolder(ItemGroupGridBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        TYPE_AD -> AdHolder(ItemGridAdBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> throw NoSuchElementException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderHolder -> holder.bind()
            is ItemHolder -> {
                holder.onItemClickListener = onItemClickListener

                holder.bind(getItem(position) as GroupItem.Group)
            }
            is AdHolder -> holder.bind(getItem(position) as GroupItem.Ad)
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is GroupItem.Title -> TYPE_TEXT
        is GroupItem.Group -> TYPE_GROUP
        is GroupItem.Ad -> TYPE_AD
        else -> super.getItemViewType(position)
    }

    fun setOnItemClickListener(listener: (View, Int) -> Unit) {
        onItemClickListener = listener
    }

    inner class HeaderHolder(val binding: ItemGridHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.tvTitle.text = binding.tvTitle.context.getString(R.string.joined_group)
        }
    }

    inner class ItemHolder(val binding: ItemGroupGridBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var onItemClickListener: (View, Int) -> Unit

        init {
            binding.rlGroup.setOnClickListener { onItemClickListener(it, adapterPosition) }
        }

        fun bind(groupItem: GroupItem.Group) = with(binding) {
            tvTitle.text = groupItem.groupName

            Glide.with(root.context)
                .load(URLs.URL_GROUP_IMAGE_PATH + groupItem.image)
                .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                .into(ivGroupImage)
        }
    }

    inner class AdHolder(val binding: ItemGridAdBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(adItem: GroupItem.Ad) = with(binding) {
            tvTitle.text = adItem.text
        }
    }

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_GROUP = 1
        private const val TYPE_AD = 2
    }
}

private class GroupGridDiffCallback : DiffUtil.ItemCallback<GroupItem>() {
    override fun areItemsTheSame(oldItem: GroupItem, newItem: GroupItem): Boolean {
        val isSameHeader = oldItem is GroupItem.Title
                && newItem is GroupItem.Title
                && oldItem == newItem
        val isSameGroup = oldItem is GroupItem.Group
                && newItem is GroupItem.Group
                && oldItem.id == newItem.id
        val isSameAdItem = oldItem is GroupItem.Ad
                && newItem is GroupItem.Ad
                && oldItem.text == newItem.text
        return isSameHeader || isSameGroup || isSameAdItem
    }

    override fun areContentsTheSame(oldItem: GroupItem, newItem: GroupItem): Boolean {
        return oldItem == newItem
    }
}