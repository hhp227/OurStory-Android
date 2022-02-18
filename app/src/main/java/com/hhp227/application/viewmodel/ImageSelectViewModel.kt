package com.hhp227.application.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.app.AppController
import com.hhp227.application.data.ImageRepository
import com.hhp227.application.dto.GalleryItem
import com.hhp227.application.dto.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ImageSelectViewModel(private val repository: ImageRepository, application: Application) : AndroidViewModel(application) {
    val state = MutableStateFlow(State())

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "ImageSelectViewModel onCleared")
    }

    private fun fetchImageList() {
        repository.getImageList(getApplication<AppController>().contentResolver).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        imageList = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        error = result.message ?: "An unexpected error occured"
                    )
                }
                is Resource.Loading -> {
                    state.value = state.value.copy(
                        isLoading = true
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    init {
        fetchImageList()
    }

    data class State(
        val isLoading: Boolean = false,
        val imageList: List<GalleryItem> = emptyList(),
        val error: String = ""
    )
}

class ImageSelectViewModelFactory(private val repository: ImageRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ImageSelectViewModel(repository, AppController.getInstance()) as T
    }
}
