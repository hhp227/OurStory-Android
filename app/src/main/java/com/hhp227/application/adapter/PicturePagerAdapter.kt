package com.hhp227.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.ItemImageFullscreenBinding
import com.hhp227.application.dto.ListItem

class PicturePagerAdapter(private val imageList: List<ListItem.Image>) : PagerAdapter() {
    override fun getCount() = imageList.size

    override fun isViewFromObject(view: View, any: Any) = view == any

    override fun instantiateItem(container: ViewGroup, position: Int) = ItemImageFullscreenBinding.inflate(LayoutInflater.from(container.context), container, false).let { binding ->
        Glide.with(container.context).load(URLs.URL_POST_IMAGE_PATH + imageList[position].image).into(binding.zivImage)
        container.addView(binding.root)
        return@let container
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        super.destroyItem(container, position, any)
        container.removeAllViews()
    }
}