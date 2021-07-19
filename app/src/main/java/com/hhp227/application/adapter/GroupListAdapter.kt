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
import com.hhp227.application.databinding.ItemEmptyBinding
import com.hhp227.application.databinding.ItemGroupListBinding
import com.hhp227.application.dto.EmptyItem
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.dto.ImageItem

class GroupListAdapter : ListAdapter<Any, GroupListAdapter.GroupViewHolder>(GroupDiffCallback()) {
    private lateinit var onItemClickListener: (View, Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        return when (viewType) {
            TYPE_GROUP -> GroupViewHolder.ItemViewHolder(ItemGroupListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            TYPE_EMPTY -> GroupViewHolder.EmptyViewHolder(ItemEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw NoSuchElementException()
        }
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        when (holder) {
            is GroupViewHolder.ItemViewHolder -> {
                holder.onItemClickListener = onItemClickListener

                holder.bind(getItem(position) as GroupItem)
            }
            is GroupViewHolder.EmptyViewHolder -> holder.bind(getItem(position) as EmptyItem)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is EmptyItem -> TYPE_EMPTY
            is GroupItem -> TYPE_GROUP
            else -> super.getItemViewType(position)
        }
    }

    fun setOnItemClickListener(listener: (view: View, position: Int) -> Unit) {
        onItemClickListener = listener
    }

    sealed class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var onItemClickListener: (View, Int) -> Unit

        class ItemViewHolder(private val binding: ItemGroupListBinding) : GroupViewHolder(binding.root) {
            fun bind(groupItem: GroupItem) = with(binding) {
                tvGroupName.text = groupItem.groupName
                tvInfo.text = groupItem.joinType.toString()

                Glide.with(root.context)
                    .load("${URLs.URL_GROUP_IMAGE_PATH}${groupItem.image}")
                    .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                    .into(binding.ivGroupImage)
            }

            init {
                binding.root.setOnClickListener { onItemClickListener.invoke(it, adapterPosition) }
            }
        }

        class EmptyViewHolder(private val binding: ItemEmptyBinding) : GroupViewHolder(binding.root) {
            fun bind(emptyItem: EmptyItem) {
                binding.tvAdd.text = emptyItem.text
                binding.ivAdd.visibility = if (emptyItem.res < 0) View.GONE else View.VISIBLE
            }
        }
    }

    companion object {
        private const val TYPE_EMPTY = 0
        private const val TYPE_GROUP = 1
    }
}

private class GroupDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        val isSameHeader = oldItem is EmptyItem
                && newItem is EmptyItem
                && oldItem.text == newItem.text
        val isSameImageItem = oldItem is ImageItem
                && newItem is ImageItem
                && oldItem.id == newItem.id
        return isSameHeader || isSameImageItem
    }
}