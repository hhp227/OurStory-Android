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
import com.hhp227.application.model.User

class FriendListAdapter : ListAdapter<User, FriendListAdapter.FriendViewHolder>(FriendDiffCallback()) {
    private lateinit var onItemClickListener: (User) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        return FriendViewHolder(ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnItemClickListener(listener: (User) -> Unit) {
        onItemClickListener = listener
    }

    inner class FriendViewHolder(private val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: User) {
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

private class FriendDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }
}