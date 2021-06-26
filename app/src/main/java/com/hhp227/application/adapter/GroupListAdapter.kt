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
import com.hhp227.application.dto.GroupItem
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_group_list.view.*

class GroupListAdapter : ListAdapter<GroupItem, GroupListAdapter.ItemViewHolder>(GroupDiffCallback()) {
    private lateinit var onItemClickListener: (View, Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_group_list, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnItemClickListener(listener: (view: View, position: Int) -> Unit) {
        onItemClickListener = listener
    }

    inner class ItemViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        init {
            containerView.setOnClickListener { onItemClickListener.invoke(it, adapterPosition) }
        }

        fun bind(groupItem: GroupItem) {
            with(containerView) {
                tv_group_name.text = groupItem.groupName
                tv_info.text = groupItem.joinType.toString()

                Glide.with(context)
                    .load("${URLs.URL_GROUP_IMAGE_PATH}${groupItem.image}")
                    .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                    .into(iv_group_image)
            }
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