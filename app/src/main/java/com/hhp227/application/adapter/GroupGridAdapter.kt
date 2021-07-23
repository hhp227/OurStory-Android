package com.hhp227.application.adapter

import android.view.LayoutInflater
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
import com.hhp227.application.dto.ImageItem


//TODO GroupFragment에서 분리해야됨
class GroupGridAdapter : ListAdapter<Any, RecyclerView.ViewHolder>(GroupGridDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        TYPE_TEXT -> HeaderHolder(ItemGridHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        TYPE_GROUP -> ItemHolder(ItemGroupGridBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        TYPE_AD -> AdHolder(ItemGridAdBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> throw NoSuchElementException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderHolder -> holder.bind(getItem(position) as String)
            is ItemHolder -> holder.bind(getItem(position) as GroupItem)
            is AdHolder -> holder.bind(getItem(position) as String)
        }
    }

    override fun getItemViewType(position: Int): Int = when {
        getItem(position) is String && getItem(position) != "광고" -> TYPE_TEXT
        getItem(position) is GroupItem -> TYPE_GROUP
        getItem(position) is String && getItem(position) == "광고" -> TYPE_AD
        else -> 0
    }

    inner class HeaderHolder(val binding: ItemGridHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.tvTitle.text = title
        }
    }

    inner class ItemHolder(val binding: ItemGroupGridBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.rlGroup.setOnClickListener {  }
        }

        fun bind(groupItem: GroupItem) = with(binding) {
            tvTitle.text = groupItem.groupName

            Glide.with(root.context)
                .load(URLs.URL_GROUP_IMAGE_PATH + groupItem.image)
                .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                .into(ivGroupImage)
            /*rlGroup.setOnClickListener {
                Intent(root.context, GroupActivity::class.java).run {
                    putExtra("group_id", groupItem.id)
                    putExtra("author_id", groupItem.authorId)
                    putExtra("group_name", groupItem.groupName)
                    startActivityForResult(this, GroupFragment.UPDATE_CODE)
                }
            }*/
        }
    }

    inner class AdHolder(val binding: ItemGridAdBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) = with(binding) {
            tvTitle.text = title
        }
    }

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_GROUP = 1
        private const val TYPE_AD = 2
    }
}

private class GroupGridDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        val isSameHeader = oldItem is String
                && newItem is String
                && oldItem == newItem
        val isSameImageItem = oldItem is ImageItem
                && newItem is ImageItem
                && oldItem.id == newItem.id
        val isSameAdItem = oldItem is String
                && newItem is String
                && oldItem == "광고"
                && newItem == "광고"
                && oldItem == newItem
        return isSameHeader || isSameImageItem || isSameAdItem
    }
}