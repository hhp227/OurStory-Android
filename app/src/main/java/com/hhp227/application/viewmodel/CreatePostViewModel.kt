package com.hhp227.application.viewmodel

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.R
import com.hhp227.application.data.PostRepository
import com.hhp227.application.helper.PhotoUriManager
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.ListItem
import com.hhp227.application.model.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CreatePostViewModel internal constructor(
    private val repository: PostRepository,
    private val photoUriManager: PhotoUriManager,
    preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private lateinit var apiKey: String

    private val post: ListItem.Post = savedStateHandle["post"] ?: ListItem.Post()

    private val type: Int = savedStateHandle["type"] ?: 0

    private val groupId: Int = savedStateHandle["group_id"] ?: 0

    val state = MutableLiveData(State(text = post.text, itemList = mutableListOf(post)))

    var photoURI: Uri? = null
        private set

    private fun insertPost(text: String) {
        repository.addPost(apiKey, groupId, text)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (state.value!!.itemList.size > IMAGE_ITEM_START_POSITION) {
                            uploadImage(IMAGE_ITEM_START_POSITION, result.data ?: -1)
                        } else {
                            state.value = state.value?.copy(
                                textError = null,
                                isLoading = false,
                                postId = result.data ?: -1
                            )
                        }
                    }
                    is Resource.Error -> {
                        state.value = state.value?.copy(
                            textError = null,
                            isLoading = false,
                            message = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(textError = null, isLoading = true)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updatePost(text: String) {
        repository.setPost(apiKey, post.id, text)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data == true) {
                            // 이미지 삭제 체크
                            deleteImages(post.id)
                            if (state.value!!.itemList.size > IMAGE_ITEM_START_POSITION) {
                                uploadImage(IMAGE_ITEM_START_POSITION, post.id)
                            } else {
                                state.value = state.value?.copy(
                                    textError = null,
                                    isLoading = false,
                                    postId = post.id
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        state.value = state.value?.copy(
                            textError = null,
                            isLoading = false,
                            message = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(textError = null, isLoading = true)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun uploadImage(position: Int, postId: Int) {
        if (postId < 0) return
        (state.value!!.itemList[position] as? ListItem.Image)?.bitmap?.also { bitmap ->
            repository.uploadImage(apiKey, postId, bitmap)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            val image = result.data // 놀고 있는 코드

                            imageUploadProcess(position, postId)
                        }
                        is Resource.Error -> {
                            state.value = state.value?.copy(
                                textError = null,
                                isLoading = false,
                                message = result.message ?: "An unexpected error occured"
                            )
                        }
                        is Resource.Loading -> {
                            state.value = state.value?.copy(textError = null, isLoading = true)
                        }
                    }
                }
                .launchIn(viewModelScope)
        } ?: imageUploadProcess(position, postId)
    }

    private fun imageUploadProcess(position: Int, postId: Int) {
        viewModelScope.launch {
            var count = position

            try {
                if (count < state.value!!.itemList.size - 1) {
                    count++
                    delay(700)
                    uploadImage(count, postId)
                } else {
                    state.value = state.value?.copy(
                        textError = null,
                        isLoading = false,
                        postId = postId
                    )
                }
            } catch (e: Exception) {
                state.value = state.value?.copy(
                    textError = null,
                    isLoading = false,
                    message = e.message ?: "An unexpected error occured"
                )
            }
        }
    }

    private fun deleteImages(postId: Int) {
        val list = post.attachment
            .imageItemList
            .takeIf(List<ListItem.Image>::isNotEmpty)
            ?.filter { state.value!!.itemList.indexOf(it) < 0 }
            ?.map(ListItem.Image::id)
            ?: emptyList()

        if (list.isNotEmpty()) {
            repository.removePostImages(apiKey, postId, list.toString())
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            val removedImageIds = result.data // 놀고있는코드

                            Log.e("TEST", "removePostImage success: ${removedImageIds}")
                        }
                        is Resource.Error -> {
                            state.value = state.value?.copy(
                                textError = null,
                                isLoading = false,
                                message = result.message ?: "An unexpected error occured"
                            )
                        }
                        is Resource.Loading -> {
                            state.value = state.value?.copy(textError = null, isLoading = true)
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun addItem(item: ListItem) {
        state.value = state.value?.copy(
            textError = null,
            itemList = (state.value!!.itemList + item).toMutableList()
        )
    }

    fun removeItem(position: Int) {
        state.value = state.value?.copy(
            textError = null,
            itemList = state.value!!
                .itemList
                .filterIndexed { index, _ -> index != position }
                .toMutableList()
        )
    }

    fun actionSend() {
        if (!TextUtils.isEmpty(state.value!!.text) || state.value!!.itemList.size > 1) {
            when (type) {
                TYPE_INSERT -> insertPost(state.value!!.text)
                TYPE_UPDATE -> updatePost(state.value!!.text)
            }
        } else {
            state.value = state.value?.copy(textError = R.string.input_content)
        }
    }

    fun getUriToSaveImage(): Uri? {
        photoURI = photoUriManager.buildNewUri()
        return photoURI
    }

    init {
        post.attachment.imageItemList.takeIf(List<ListItem.Image>::isNotEmpty)?.also(state.value!!.itemList::addAll)
        viewModelScope.launch {
            preferenceManager.getUserFlow()
                .collectLatest { user ->
                    apiKey = user?.apiKey ?: ""
                }
        }
        Log.e("TEST", "type: ${type}, post: ${post}")
    }

    companion object {
        const val TYPE_INSERT = 0
        const val TYPE_UPDATE = 1
        const val IMAGE_ITEM_START_POSITION = 1
    }

    data class State(
        val text: String = "",
        val textError: Int? = null,
        val itemList: MutableList<ListItem> = mutableListOf(),
        val isLoading: Boolean = false,
        val postId: Int = -1,
        val message: String = ""
    )
}

class CreatePostViewModelFactory(
    private val repository: PostRepository,
    private val photoUriManager: PhotoUriManager,
    private val preferenceManager: PreferenceManager,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(CreatePostViewModel::class.java)) {
            return CreatePostViewModel(repository, photoUriManager, preferenceManager, handle) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}