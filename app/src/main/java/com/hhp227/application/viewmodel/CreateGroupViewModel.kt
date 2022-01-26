package com.hhp227.application.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.app.AppController
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.IOException

class CreateGroupViewModel : ViewModel() {
    val state = MutableStateFlow(State())

    val repository = GroupRepository()

    lateinit var uri: Uri

    lateinit var currentPhotoPath: String

    val apiKey: String by lazy { AppController.getInstance().preferenceManager.user.apiKey }

    var bitMap: Bitmap? = null

    var joinType = false

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "CreateGroupViewModel onCleared")
    }

    fun setBitmap(bitmapUtil: BitmapUtil) {
        bitMap = try {
            bitmapUtil.bitmapResize(uri, 200)?.let {
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

    fun createGroup(title: String, description: String, joinType: String) {

        // TODO addGroup과 중복체크가 일어나서 별로 안좋은 코드 추후 리팩토링 해볼것
        if (title.isNotEmpty() && description.isNotEmpty()) {
            bitMap?.also {
                repository.addGroupImage(apiKey, it).onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            state.value = state.value.copy(
                                isLoading = false,
                                image = result.data
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
            } ?: addGroup(title, description, joinType, null)
        } else {
            state.value = state.value.copy(
                error = "input_correct"
            )
        }
    }

    fun addGroup(title: String, description: String, joinType: String, image: String?) {
        if (title.isNotEmpty() && description.isNotEmpty()) {
            repository.addGroup(apiKey, title, description, joinType, image).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value.copy(
                            isLoading = false,
                            group = result.data
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
        } else {
            state.value = state.value.copy(
                error = "input_correct"
            )
        }
    }

    data class State(
        val isLoading: Boolean = false,
        val image: String? = null,
        val group: GroupItem.Group? = null,
        val error: String = ""
    )
}