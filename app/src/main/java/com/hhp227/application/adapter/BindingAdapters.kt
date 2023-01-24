package com.hhp227.application.adapter

import androidx.databinding.BindingAdapter
import androidx.paging.PagingData
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.model.ListItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@BindingAdapter("submitData")
fun submitData(v: RecyclerView, data: PagingData<ListItem.Post>?) {
    if (data != null && data != PagingData.empty<ListItem.Post>()) {
        CoroutineScope(Dispatchers.Main).launch {
            ((v.adapter as? ConcatAdapter)?.adapters?.first() as? PostPagingDataAdapter)?.submitData(data)
        }
    }
}

@BindingAdapter("payload")
fun bindPayload(v: RecyclerView, payload: ListItem.Post?) {
    val concatAdapter = v.adapter as? ConcatAdapter

    (concatAdapter?.adapters?.first() as? PostPagingDataAdapter)?.also { adapter ->
        payload?.also(adapter::updatePost)
    }
}