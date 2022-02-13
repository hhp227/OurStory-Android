package com.hhp227.application.viewmodel

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.app.AppController
import com.hhp227.application.data.PostRepository
import com.hhp227.application.dto.ImageItem
import com.hhp227.application.dto.PostItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONArray

class WriteViewModel(private val repository: PostRepository, savedStateHandle: SavedStateHandle) : ViewModel() {
    val state = MutableStateFlow(State())

    val apiKey: String by lazy { AppController.getInstance().preferenceManager.user.apiKey }

    val post: PostItem.Post

    val type: Int

    val groupId: Int

    lateinit var currentPhotoPath: String

    lateinit var photoURI: Uri


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
        (state.value.itemList[position] as? ImageItem)?.bitmap?.also { bitmap ->
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
        val imageIdJsonArray = JSONArray().also { jsonArray ->
            post.imageItemList.takeIf(List<ImageItem>::isNotEmpty)?.forEach { i ->
                if (state.value.itemList.indexOf(i) == -1)
                    jsonArray.put(i.id)
            } ?: return
        }

        // TODO imageIdJsonArray가 비어있으면 return하게 만들것, removePostImages안의 try catch문 지우게 처리할것 (이미지 삭제가 없을경우만 에러가 발생)
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

    fun addItem(item: PostItem) {
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

    init {
        type = savedStateHandle.get<Int>("type") ?: 0
        groupId = savedStateHandle.get<Int>("group_id") ?: 0
        post = savedStateHandle.get<PostItem.Post>("post") ?: PostItem.Post()

        state.value.itemList.add(post)
        post.imageItemList.takeIf(List<ImageItem>::isNotEmpty)?.also(state.value.itemList::addAll)
    }

    companion object {
        const val TYPE_INSERT = 0
        const val TYPE_UPDATE = 1
        const val IMAGE_ITEM_START_POSITION = 1
    }

    data class State(
        val isLoading: Boolean = false,
        val itemList: MutableList<PostItem> = mutableListOf(),
        val postId: Int = -1,
        val error: String = ""
    )
}

class WriteViewModelFactory(
    private val repository: PostRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(WriteViewModel::class.java)) {
            return WriteViewModel(repository, handle) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}