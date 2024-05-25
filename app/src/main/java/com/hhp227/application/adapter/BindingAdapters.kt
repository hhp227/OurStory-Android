package com.hhp227.application.adapter

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.navigation.findNavController
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hhp227.application.R
import com.hhp227.application.fragment.PostDetailFragmentDirections
import com.hhp227.application.helper.CustomDecoration
import com.hhp227.application.model.ListItem
import com.hhp227.application.util.URLs
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

@BindingAdapter(value = ["profileImageFromUrl", "profileImageFromBitmap"])
fun bindProfileImageFromUrlOrBitmap(view: ImageView, imageUrl: String?, bitmap: Bitmap?) {
    val any: Any? = when {
        bitmap != null -> bitmap
        !imageUrl.isNullOrEmpty() -> imageUrl
        else -> null
    }

    view.load(any) {
        placeholder(R.drawable.profile_img_circle)
        error(R.drawable.profile_img_circle)
        transformations(CircleCropTransformation())
    }
}

@BindingAdapter("imageFromUrl")
fun bindImageFromUrl(view: ImageView, any: Any) {
    view.load(any) {
        placeholder(R.drawable.ic_launcher)
        error(R.drawable.ic_launcher) // temp image
        crossfade(150)
    }
}

@BindingAdapter(
    value = ["imageList", "onImageClick"],
    requireAll = false
)
fun bindImageList(view: LinearLayout, list: List<ListItem.Image>, onImageClickListener: ReplyListAdapter.OnImageClickListener) {
    view.removeAllViews()
    for (index in list.indices) {
        ImageView(view.context).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_XY

            setPadding(0, 0, 0, 30)
            load("${URLs.URL_POST_IMAGE_PATH}${list[index].image}") {
                error(R.drawable.ic_launcher)
            }
            setOnClickListener { onImageClickListener.onImageClick(list, index) }
        }.also { view.addView(it) } // apply().also() -> run()으로 바꿀수 있음
    }
}

@BindingAdapter("spanCount")
fun bindSpanCount(view: RecyclerView, spanCount: Int) {
    (view.layoutManager as? GridLayoutManager)?.also { gridLayoutManager ->
        val adapter = (view.adapter as ConcatAdapter).adapters.first()
        gridLayoutManager.spanCount = spanCount
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == adapter.itemCount) spanCount
                else when (adapter.getItemViewType(position)) {
                    GroupGridAdapter.TYPE_GROUP, GroupGridAdapter.TYPE_AD -> 1
                    else -> spanCount
                }
            }
        }
    }
    view.invalidateItemDecorations()
}

@BindingAdapter(
    value = ["dividerHeight", "dividerPadding", "dividerColor"],
    requireAll = false
)
fun RecyclerView.setDivider(dividerHeight: Float?, dividerPadding: Float?, @ColorInt dividerColor: Int?) {
    val decoration = CustomDecoration(
        height = dividerHeight ?: 0f,
        padding = dividerPadding ?: 0f,
        color = dividerColor ?: Color.TRANSPARENT
    )

    addItemDecoration(decoration)
}