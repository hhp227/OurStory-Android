package com.hhp227.application.adapter

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hhp227.application.R
import com.hhp227.application.model.ListItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@BindingAdapter("submitData")
fun submitData(v: RecyclerView, data: PagingData<Nothing>?) {
    if (data != null && data != PagingData.empty<Nothing>()) {
        CoroutineScope(Dispatchers.Main).launch {
            ((v.adapter as? ConcatAdapter)?.adapters?.first() as? PagingDataAdapter<Nothing, Nothing>)?.submitData(data)
        }
    }
}

@BindingAdapter("submitList")
fun submitList(v: RecyclerView, list: List<Nothing>) {
    (v.adapter as? ListAdapter<*, *>)?.submitList(list)
}

@BindingAdapter("payload")
fun bindPayload(v: RecyclerView, payload: ListItem.Post?) {
    val concatAdapter = v.adapter as? ConcatAdapter

    (concatAdapter?.adapters?.first() as? PostPagingDataAdapter)?.also { adapter ->
        payload?.also(adapter::updatePost)
    }
}

@BindingAdapter("error")
fun textInputError(e: EditText, error: Int?) {
    error?.let { e.error = e.context.getString(it) }
}

@BindingAdapter("profileImageFromUrl")
fun bindProfileImageFromUrl(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        view.load(imageUrl) {
            placeholder(R.drawable.profile_img_circle)
            error(R.drawable.profile_img_circle)
            transformations(CircleCropTransformation())
        }
    }
}

@BindingAdapter("imageFromUrl")
fun bindImageFromUrl(view: ImageView, any: Any) {
    view.load(any) {
        error(R.drawable.ic_launcher) // temp image
    }
}