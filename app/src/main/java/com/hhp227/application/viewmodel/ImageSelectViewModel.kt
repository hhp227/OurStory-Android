package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import com.hhp227.application.dto.GalleryItem

class ImageSelectViewModel : ViewModel() {
    val imageList = mutableListOf<GalleryItem>()
}