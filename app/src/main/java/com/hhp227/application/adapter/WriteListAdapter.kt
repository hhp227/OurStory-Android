package com.hhp227.application.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.InputContentsBinding
import com.hhp227.application.databinding.InputTextBinding
import com.hhp227.application.dto.ImageItem

class WriteListAdapter : ListAdapter<Any, WriteListAdapter.WriteViewHolder>(WriteDiffCallback()) {
    private lateinit var onItemClickListener: OnItemClickListener

    lateinit var headerHolder: WriteViewHolder.HeaderHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WriteViewHolder {
        return when (viewType) {
            TYPE_TEXT -> WriteViewHolder.HeaderHolder(
                InputTextBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            ).also { headerHolder = it }
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
        when (holder) {
            is WriteViewHolder.HeaderHolder -> holder.bind(getItem(position).toString())
            is WriteViewHolder.ImageHolder -> {
                holder.onItemClickListener = onItemClickListener

                holder.bind(getItem(position) as ImageItem)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is ImageItem) TYPE_IMAGE else TYPE_TEXT
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun interface OnItemClickListener {
        fun onItemClick(v: View, p: Int)
    }

    sealed class WriteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var onItemClickListener: OnItemClickListener

        class HeaderHolder(val binding: InputTextBinding) : WriteViewHolder(binding.root) {
            fun bind(text: String) {
                binding.etText.setText(text)
            }
        }
        class ImageHolder(val binding: InputContentsBinding) : WriteViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener { v ->
                    onItemClickListener.onItemClick(v, adapterPosition)
                }
            }

            fun bind(imageItem: ImageItem) {
                with(binding) {
                    Glide.with(root)
                        .load(when {
                            imageItem.bitmap != null -> imageItem.bitmap
                            imageItem.image != null -> URLs.URL_POST_IMAGE_PATH + imageItem.image
                            else -> null
                        })
                        .into(binding.ivPreview)
                }
            }
        }
    }

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_IMAGE = 1
    }
}

private class WriteDiffCallback : DiffUtil.ItemCallback<Any>() {
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
        return isSameHeader || isSameImageItem
    }
}