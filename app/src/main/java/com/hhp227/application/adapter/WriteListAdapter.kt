package com.hhp227.application.adapter

import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hhp227.application.util.URLs
import com.hhp227.application.databinding.InputContentsBinding
import com.hhp227.application.databinding.InputTextBinding
import com.hhp227.application.model.ListItem

class WriteListAdapter : ListAdapter<ListItem, WriteListAdapter.WriteViewHolder>(WriteDiffCallback()) {
    private lateinit var onWriteListAdapterListener: OnWriteListAdapterListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WriteViewHolder {
        return when (viewType) {
            TYPE_TEXT -> WriteViewHolder.HeaderHolder(
                InputTextBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_IMAGE -> WriteViewHolder.ImageHolder(
                InputContentsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw NoSuchElementException()
        }
    }

    override fun onBindViewHolder(holder: WriteViewHolder, position: Int) {
        holder.onWriteListAdapterListener = onWriteListAdapterListener

        when (holder) {
            is WriteViewHolder.HeaderHolder -> holder.bind((getItem(position) as ListItem.Post))
            is WriteViewHolder.ImageHolder -> holder.bind(getItem(position) as ListItem.Image)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is ListItem.Image) TYPE_IMAGE else TYPE_TEXT
    }

    fun setOnWriteListAdapterListener(listener: OnWriteListAdapterListener) {
        onWriteListAdapterListener = listener
    }

    interface OnWriteListAdapterListener {
        fun onItemClick(v: View, p: Int)

        fun onValueChange(e: Editable?)
    }

    sealed class WriteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var onWriteListAdapterListener: OnWriteListAdapterListener

        class HeaderHolder(val binding: InputTextBinding) : WriteViewHolder(binding.root) {
            fun bind(post: ListItem.Post) {
                binding.text = post.text
                binding.onValueChange = onWriteListAdapterListener::onValueChange

                binding.executePendingBindings()
            }
        }
        class ImageHolder(val binding: InputContentsBinding) : WriteViewHolder(binding.root) {
            fun bind(imageItem: ListItem.Image) {
                with(binding) {
                    ivPreview.load(when {
                        imageItem.bitmap != null -> imageItem.bitmap
                        imageItem.image != null -> URLs.URL_POST_IMAGE_PATH + imageItem.image
                        else -> null
                    })
                }
            }

            init {
                binding.root.setOnClickListener { v ->
                    onWriteListAdapterListener.onItemClick(v, bindingAdapterPosition)
                }
            }
        }
    }

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_IMAGE = 1
    }
}

private class WriteDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem) = newItem == oldItem

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        val isSameHeader = oldItem is ListItem.Post
                && newItem is ListItem.Post
                && oldItem.id == newItem.id
        val isSameImageItem = oldItem is ListItem.Image
                && newItem is ListItem.Image
                && oldItem.id == newItem.id
        return isSameHeader || isSameImageItem
    }
}