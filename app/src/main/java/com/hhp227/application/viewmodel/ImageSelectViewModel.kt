package com.hhp227.application.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hhp227.application.app.AppController
import com.hhp227.application.data.ImageRepository
import com.hhp227.application.model.GalleryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ImageSelectViewModel internal constructor(private val repository: ImageRepository, application: Application) : AndroidViewModel(application) {
    val state = MutableStateFlow(State())

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "ImageSelectViewModel onCleared")
    }

    private fun fetchImageList() = repository.getImageDataStream(getApplication<AppController>().contentResolver)
        .cachedIn(viewModelScope)
        .onEach { data ->
            state.value = state.value.copy(
                isLoading = false,
                data = data
            )
        }
        .launchIn(viewModelScope)

    init {
        fetchImageList()
    }

    data class State(
        val isLoading: Boolean = false,
        val data: PagingData<GalleryItem> = PagingData.empty(),
    )
}

class ImageSelectViewModelFactory(private val repository: ImageRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ImageSelectViewModel(repository, AppController.getInstance()) as T
    }
}
