package com.hhp227.application.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.app.AppController
import com.hhp227.application.data.PostRepository
import com.hhp227.application.dto.ListItem
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.dto.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.IOException

class CreatePostViewModel(private val repository: PostRepository, savedStateHandle: SavedStateHandle) : ViewModel() {
    private val apiKey: String by lazy { AppController.getInstance().preferenceManager.user.apiKey }

    private val post: ListItem.Post = savedStateHandle.get("post") ?: ListItem.Post()

    private val type: Int = savedStateHandle.get("type") ?: 0

    private val groupId: Int = savedStateHandle.get("group_id") ?: 0

    lateinit var currentPhotoPath: String

    lateinit var photoURI: Uri

    val state = MutableStateFlow(State())

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "WriteViewModel onCleared")
    }

    private fun insertPost(text: String) {
        repository.addPost(apiKey, groupId, text).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (state.value.itemList.size > IMAGE_ITEM_START_POSITION) {
                        uploadImage(IMAGE_ITEM_START_POSITION, result.data ?: -1)
                    } else {
                        state.value = state.value.copy(
                            isLoading = false,
                            postId = result.data ?: -1
                        )
                    }
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

    private fun updatePost(text: String) {
        repository.setPost(apiKey, post.id, text).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val postId = result.data ?: -1

                    // 이미지 삭제 체크
                    deleteImages(postId)
                    if (state.value.itemList.size > IMAGE_ITEM_START_POSITION) {
                        uploadImage(IMAGE_ITEM_START_POSITION, postId)
                    } else {
                        state.value = state.value.copy(
                            isLoading = false,
                            postId = postId
                        )
                    }
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

    private fun uploadImage(position: Int, postId: Int) {
        if (postId < 0)
            return
        (state.value.itemList[position] as? ListItem.Image)?.bitmap?.also { bitmap ->
            repository.addPostImage(apiKey, postId, bitmap).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        val image = result.data // 놀고 있는 코드

                        imageUploadProcess(position, postId)
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
        } ?: imageUploadProcess(position, postId)
    }

    private fun imageUploadProcess(position: Int, postId: Int) {
        viewModelScope.launch {
            var count = position

            try {
                if (count < state.value.itemList.size - 1) {
                    count++
                    delay(700)
                    uploadImage(count, postId)
                } else {
                    state.value = state.value.copy(
                        isLoading = false,
                        postId = postId
                    )
                }
            } catch (e: Exception) {
                state.value = state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occured"
                )
            }
        }
    }

    private fun deleteImages(postId: Int) {
        val imageIdJsonArray = JSONArray()

        post.imageItemList.takeIf(List<ListItem.Image>::isNotEmpty)?.forEach { i ->
            if (state.value.itemList.indexOf(i) < 0)
                imageIdJsonArray.put(i.id)
        }
        if (imageIdJsonArray.length() > 0) {
            repository.removePostImages(apiKey, postId, imageIdJsonArray).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        val removedImageIds = result.data // 놀고있는코드

                        Log.e("TEST", "removePostImage success: ${removedImageIds}")
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
    }

    fun addItem(item: ListItem) {
        state.value.itemList.add(item)
    }

    fun removeItem(position: Int) {
        state.value.itemList.removeAt(position)
    }

    fun actionSend(text: String) {
        if (text.isNotEmpty() || state.value.itemList.size > 1) {
            when (type) {
                TYPE_INSERT -> insertPost(text)
                TYPE_UPDATE -> updatePost(text)
            }
        } else {
            state.value = state.value.copy(error = "내용을 입력하세요.")
        }
    }

    fun getBitMap(bitmapUtil: BitmapUtil): Bitmap? {
        return try {
            bitmapUtil.bitmapResize(photoURI, 200)?.let {
                val ei = ExifInterface(currentPhotoPath)
                val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

                bitmapUtil.rotateImage(it, when (orientation) {
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

    init {
        state.value.itemList.add(post)
        post.imageItemList.takeIf(List<ListItem.Image>::isNotEmpty)?.also(state.value.itemList::addAll)
    }

    companion object {
        const val TYPE_INSERT = 0
        const val TYPE_UPDATE = 1
        const val IMAGE_ITEM_START_POSITION = 1
    }

    data class State(
        val isLoading: Boolean = false,
        val itemList: MutableList<ListItem> = mutableListOf(),
        val postId: Int = -1,
        val error: String = ""
    )
}

class CreatePostViewModelFactory(
    private val repository: PostRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(CreatePostViewModel::class.java)) {
            return CreatePostViewModel(repository, handle) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}