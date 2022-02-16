package com.hhp227.application.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.app.AppController
import com.hhp227.application.data.UserRepository
import com.hhp227.application.dto.UserItem
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.IOException

class MyInfoViewModel internal constructor(private val repository: UserRepository) : ViewModel() {
    val user: UserItem by lazy { AppController.getInstance().preferenceManager.user }

    lateinit var currentPhotoPath: String

    lateinit var photoURI: Uri

    val state = MutableStateFlow(State())

    var bitmap: Bitmap? = null

    private fun updateUserProfile(imageUrl: String = "null") {
        repository.setUserProfile(user.apiKey, imageUrl).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        imageUrl = result.data
                    )
                }
                is Resource.Error -> {
                    state.value = state.value.copy(
                        isLoading = false,
                        error = result.message ?: "An unexpected error occured"
                    )
                }
                is Resource.Loading -> {
                    state.value = state.value.copy(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun uploadImage() {
        bitmap?.let {
            repository.addProfileImage(user.apiKey, it).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        val imageUrl = result.data ?: "null"

                        updateUserProfile(imageUrl)
                    }
                    is Resource.Error -> {
                        state.value = state.value.copy(
                            isLoading = false,
                            error = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value.copy(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        } ?: updateUserProfile()
    }

    fun setBitmap(bitmapUtil: BitmapUtil) {
        bitmap = try {
            bitmapUtil.bitmapResize(photoURI, 200)?.let {
                val ei = currentPhotoPath.let(::ExifInterface)
                val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                return@let bitmapUtil.rotateImage(it, when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90F
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180F
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270F
                    else -> 0F
                })
            }
        } catch (e: IOException) {
            null
        }
    }

    fun resetState() {
        state.value = State()
    }

    data class State(
        val isLoading: Boolean = false,
        val imageUrl: String? = null,
        val error: String = ""
    )
}

class MyInfoViewModelFactory(
    private val repository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyInfoViewModel::class.java)) {
            return MyInfoViewModel(repository) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}