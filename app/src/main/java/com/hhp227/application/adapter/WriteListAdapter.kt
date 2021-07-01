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

class WriteListAdapter : ListAdapter<Any, WriteListAdapter.SealedAdapterViewHolder>(WriteDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SealedAdapterViewHolder {
        return when (viewType) {
            TYPE_TEXT -> SealedAdapterViewHolder.HeaderHolder(
                InputTextBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_IMAGE -> SealedAdapterViewHolder.ImageHolder(
                InputContentsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw NoSuchElementException()
        }
    }

    override fun onBindViewHolder(holder: SealedAdapterViewHolder, position: Int) {
        when (holder) {
            is SealedAdapterViewHolder.HeaderHolder -> holder.bind(getItem(position).toString())
            is SealedAdapterViewHolder.ImageHolder -> holder.bind(getItem(position) as ImageItem)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is ImageItem) TYPE_IMAGE else TYPE_TEXT
    }

    sealed class SealedAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class HeaderHolder(val binding: InputTextBinding) : SealedAdapterViewHolder(binding.root) {
            fun bind(text: String) {
                binding.etText.setText(text)
            }
        }
        class ImageHolder(val binding: InputContentsBinding) : SealedAdapterViewHolder(binding.root) {
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

    @SuppressLint("DiffUtilEquals")
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