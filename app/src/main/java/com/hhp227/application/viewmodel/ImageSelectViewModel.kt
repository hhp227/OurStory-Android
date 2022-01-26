package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hhp227.application.data.ImageRepository
import com.hhp227.application.dto.GalleryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

class ImageSelectViewModel(private val repository: ImageRepository) : ViewModel() {
    private val state = MutableStateFlow(State())

    val imageList = mutableListOf<GalleryItem>()

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "ImageSelectViewModel onCleared")
    }

    fun fetchImageList() {
        repository.getImageList().onEach { result ->

        }
    }

    init {
        fetchImageList()
    }

    data class State(
        val isLoading: Boolean = false,
        val imageList: List<GalleryItem> = mutableListOf(),
        val error: String = ""
    )
}

class ImageSelectViewModelFactory(private val repository: ImageRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ImageSelectViewModel(repository) as T
    }
}
