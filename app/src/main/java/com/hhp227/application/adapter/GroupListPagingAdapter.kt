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
import com.hhp227.application.databinding.ItemEmptyBinding
import com.hhp227.application.databinding.ItemGroupListBinding
import com.hhp227.application.model.GroupItem

class GroupListPagingAdapter : PagingDataAdapter<GroupItem, RecyclerView.ViewHolder>(GroupItemDiffCallback()) {
    private lateinit var onItemClickListener: (View, Int) -> Unit

    val loadState: LiveData<CombinedLoadStates> get() = loadStateFlow.asLiveData()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GroupViewHolder.ItemViewHolder -> {
                holder.onItemClickListener = onItemClickListener

                holder.bind(getItem(position) as GroupItem.Group)
            }
            is GroupViewHolder.EmptyViewHolder -> holder.bind(getItem(position) as GroupItem.Empty)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_GROUP -> GroupViewHolder.ItemViewHolder(
                ItemGroupListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            TYPE_EMPTY -> GroupViewHolder.EmptyViewHolder(
                ItemEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> throw NoSuchElementException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GroupItem.Empty -> TYPE_EMPTY
            is GroupItem -> TYPE_GROUP
            else -> super.getItemViewType(position)
        }
    }

    fun setOnItemClickListener(listener: (View, Int) -> Unit) {
        onItemClickListener = listener
    }

    sealed class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var onItemClickListener: (View, Int) -> Unit

        class ItemViewHolder(private val binding: ItemGroupListBinding) : GroupViewHolder(binding.root) {
            fun bind(group: GroupItem.Group) = with(binding) {
                item = group

                executePendingBindings()
            }

            init {
                binding.root.setOnClickListener { onItemClickListener.invoke(it, bindingAdapterPosition) }
            }
        }

        class EmptyViewHolder(private val binding: ItemEmptyBinding) : GroupViewHolder(binding.root) {
            fun bind(empty: GroupItem.Empty) = with(binding) {
                item = empty
                addString = root.context.getString(empty.res)

                executePendingBindings()
            }
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