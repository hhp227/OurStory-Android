package com.hhp227.application.adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.model.GroupItem

class GroupListPagingAdapter : PagingDataAdapter<GroupItem, RecyclerView.ViewHolder>(GroupItemDiffCallback()) {
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GroupItem.Empty -> TYPE_EMPTY
            is GroupItem -> TYPE_GROUP
            else -> super.getItemViewType(position)
        }
    }

    companion object {
        private const val TYPE_EMPTY = 0
        private const val TYPE_GROUP = 1
    }
}

private class GroupItemDiffCallback : DiffUtil.ItemCallback<GroupItem>() {
    override fun areItemsTheSame(oldItem: GroupItem, newItem: GroupItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: GroupItem, newItem: GroupItem): Boolean {
        val isSameHeader = oldItem is GroupItem.Empty
                && newItem is GroupItem.Empty
                && oldItem.strRes == newItem.strRes
        val isSameGroupItem = oldItem is GroupItem.Group
                && newItem is GroupItem.Group
                && oldItem.id == newItem.id
        return isSameHeader || isSameGroupItem
    }
}