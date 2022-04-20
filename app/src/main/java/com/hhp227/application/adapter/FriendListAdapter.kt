package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.app.URLs
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

            Glide.with(binding.ivProfileImage)
                .load(URLs.URL_USER_PROFILE_IMAGE + item.profileImage)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(binding.ivProfileImage)
        }

        init {
            itemView.setOnClickListener { onItemClickListener(getItem(bindingAdapterPosition)) }
        }
    }
}

private class FriendDiffCallback : DiffUtil.ItemCallback<UserItem>() {
    override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
        return oldItem == newItem
    }
}