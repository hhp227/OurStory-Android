package com.hhp227.application.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.hhp227.application.R
import com.bumptech.glide.Glide
import com.hhp227.application.app.URLs
import com.bumptech.glide.request.RequestOptions
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.hhp227.application.databinding.ItemMemberBinding
import com.hhp227.application.dto.UserItem

class MemberGridAdapter : ListAdapter<UserItem, MemberGridAdapter.ItemHolder>(MemberDiffCallback()) {
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    inner class ItemHolder(private val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(memberItem: UserItem) {
            binding.tvNameUser.text = memberItem.name

            Glide.with(itemView.context)
                .load(URLs.URL_USER_PROFILE_IMAGE + memberItem.profileImage)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_square))
                .into(binding.ivProfileImage)
        }

        init {
            itemView.setOnClickListener { v -> onItemClickListener?.onItemClick(v, bindingAdapterPosition) }
        }
    }

    fun interface OnItemClickListener {
        fun onItemClick(v: View?, p: Int)
    }
}

private class MemberDiffCallback : DiffUtil.ItemCallback<UserItem>() {
    override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
        return oldItem == newItem
    }
}