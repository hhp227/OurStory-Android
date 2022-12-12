package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hhp227.application.R
import com.hhp227.application.util.URLs
import com.hhp227.application.databinding.ItemFriendBinding
import com.hhp227.application.dto.UserItem

class FriendListAdapter : ListAdapter<UserItem, FriendListAdapter.FriendViewHolder>(FriendDiffCallback()) {
    private lateinit var onItemClickListener: (UserItem) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        return FriendViewHolder(ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnItemClickListener(listener: (UserItem) -> Unit) {
        onItemClickListener = listener
    }

    inner class FriendViewHolder(private val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserItem) {
            binding.tvItem.text = item.name

            binding.ivProfileImage.load(URLs.URL_USER_PROFILE_IMAGE + item.profileImage) {
                placeholder(R.drawable.profile_img_circle)
                error(R.drawable.profile_img_circle)
                transformations(CircleCropTransformation())
            }
        }

        init {
            itemView.setOnClickListener { onItemClickListener(getItem(bindingAdapterPosition)) }
        }
    }
}

private class FriendDiffCallback : DiffUtil.ItemCallback<UserItem>() {
    override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
        return oldItem.id == newItem.id
    }
}