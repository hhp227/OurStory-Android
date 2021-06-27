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
import com.hhp227.application.databinding.ItemGroupListBinding
import com.hhp227.application.dto.GroupItem

class GroupListAdapter : ListAdapter<GroupItem, GroupListAdapter.ItemViewHolder>(GroupDiffCallback()) {
    private lateinit var onItemClickListener: (View, Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(ItemGroupListBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnItemClickListener(listener: (view: View, position: Int) -> Unit) {
        onItemClickListener = listener
    }

    inner class ItemViewHolder(private val binding: ItemGroupListBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { onItemClickListener.invoke(it, adapterPosition) }
        }

        fun bind(groupItem: GroupItem) = with(binding) {
            tvGroupName.text = groupItem.groupName
            tvInfo.text = groupItem.joinType.toString()

            Glide.with(root.context)
                .load("${URLs.URL_GROUP_IMAGE_PATH}${groupItem.image}")
                .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                .into(binding.ivGroupImage)
        }
    }
}

private class GroupDiffCallback : DiffUtil.ItemCallback<GroupItem>() {
    override fun areItemsTheSame(oldItem: GroupItem, newItem: GroupItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: GroupItem, newItem: GroupItem): Boolean {
        return oldItem == newItem
    }
}