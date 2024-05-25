package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.paging.CombinedLoadStates
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.databinding.ItemGridAdBinding
import com.hhp227.application.databinding.ItemGridHeaderBinding
import com.hhp227.application.databinding.ItemGroupGridBinding
import com.hhp227.application.model.GroupItem

class GroupGridAdapter : PagingDataAdapter<GroupItem, RecyclerView.ViewHolder>(GroupGridDiffCallback()) {
    private lateinit var onItemClickListener: (View, Int) -> Unit

    val loadState: LiveData<CombinedLoadStates> get() = loadStateFlow.asLiveData()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            TYPE_TEXT -> HeaderHolder(
                ItemGridHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_GROUP -> ItemHolder(
                ItemGroupGridBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_AD -> AdHolder(
                ItemGridAdBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw NoSuchElementException()
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderHolder -> holder.bind(getItem(position) as GroupItem.Title)
            is ItemHolder -> holder.bind(getItem(position) as GroupItem.Group)
            is AdHolder -> holder.bind(getItem(position) as GroupItem.Ad)
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is GroupItem.Title -> TYPE_TEXT
        is GroupItem.Group -> TYPE_GROUP
        is GroupItem.Ad -> TYPE_AD
        else -> super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        return super.getItemCount() - 1
    }

    fun setOnItemClickListener(listener: (View, Int) -> Unit) {
        onItemClickListener = listener
    }

    inner class HeaderHolder(val binding: ItemGridHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: GroupItem.Title) {
            binding.tvTitle.text = binding.tvTitle.context.getString(title.resId)
        }
    }

    inner class ItemHolder(val binding: ItemGroupGridBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.rlGroup.setOnClickListener { onItemClickListener(it, bindingAdapterPosition) }
        }

        fun bind(groupItem: GroupItem.Group) = with(binding) {
            group = groupItem

            executePendingBindings()
        }
    }

    inner class AdHolder(val binding: ItemGridAdBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(adItem: GroupItem.Ad) = with(binding) {
            ad = adItem

            executePendingBindings()
        }
    }

    companion object {
        const val TYPE_TEXT = 0
        const val TYPE_GROUP = 1
        const val TYPE_AD = 2
    }
}

private class GroupGridDiffCallback : DiffUtil.ItemCallback<GroupItem>() {
    override fun areItemsTheSame(oldItem: GroupItem, newItem: GroupItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: GroupItem, newItem: GroupItem): Boolean {
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
}