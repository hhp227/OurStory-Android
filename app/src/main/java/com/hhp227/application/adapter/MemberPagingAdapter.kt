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
import com.hhp227.application.databinding.ItemMemberBinding
import com.hhp227.application.model.User

class MemberPagingAdapter : PagingDataAdapter<User, MemberPagingAdapter.ItemHolder>(MemberDiffCallback()) {
    private var onItemClickListener: OnItemClickListener? = null

    val loadState: LiveData<CombinedLoadStates> get() = loadStateFlow.asLiveData()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        getItem(position)?.let(holder::bind)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    fun updateProfileImages(user: User) {
        snapshot().items
            .forEach { member ->
                if (member.id == user.id) {
                    member.profileImage = user.profileImage
                }
            }
    }

    inner class ItemHolder(private val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(member: User) {
            binding.item = member

            binding.executePendingBindings()
        }

        init {
            itemView.setOnClickListener { v -> onItemClickListener?.onItemClick(v, bindingAdapterPosition) }
        }
    }

    fun interface OnItemClickListener {
        fun onItemClick(v: View?, p: Int)
    }
}

private class MemberDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }
}