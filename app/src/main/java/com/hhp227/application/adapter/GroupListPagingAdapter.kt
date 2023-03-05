package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hhp227.application.R
import com.hhp227.application.databinding.ItemEmptyBinding
import com.hhp227.application.databinding.ItemGroupListBinding
import com.hhp227.application.model.GroupItem
import com.hhp227.application.util.URLs

class GroupListPagingAdapter : PagingDataAdapter<GroupItem, RecyclerView.ViewHolder>(GroupItemDiffCallback()) {
    private lateinit var onItemClickListener: (View, Int) -> Unit

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GroupListAdapter.GroupViewHolder.ItemViewHolder -> {
                holder.onItemClickListener = onItemClickListener

                holder.bind(getItem(position) as GroupItem.Group)
            }
            is GroupListAdapter.GroupViewHolder.EmptyViewHolder -> holder.bind(getItem(position) as GroupItem.Empty)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_GROUP -> GroupListAdapter.GroupViewHolder.ItemViewHolder(ItemGroupListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            TYPE_EMPTY -> GroupListAdapter.GroupViewHolder.EmptyViewHolder(ItemEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
            fun bind(groupItem: GroupItem.Group) = with(binding) {
                tvGroupName.text = groupItem.groupName
                tvInfo.text = groupItem.joinType.toString()

                ivGroupImage.load("${URLs.URL_GROUP_IMAGE_PATH}${groupItem.image}") {
                    error(R.drawable.ic_launcher)
                }
            }

            init {
                binding.root.setOnClickListener { onItemClickListener.invoke(it, bindingAdapterPosition) }
            }
        }

        class EmptyViewHolder(private val binding: ItemEmptyBinding) : GroupViewHolder(binding.root) {
            fun bind(emptyItem: GroupItem.Empty) {
                binding.tvAdd.text = binding.tvAdd.context.getString(emptyItem.strRes)
                binding.ivAdd.visibility = if (emptyItem.res < 0) View.GONE else View.VISIBLE
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