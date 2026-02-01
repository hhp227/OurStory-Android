package com.hhp227.application.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhp227.application.R
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.helper.PhotoUriManager
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CreateGroupViewModel internal constructor(
    private val repository: GroupRepository,
    private val photoUriManager: PhotoUriManager,
    preferenceManager: PreferenceManager
) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableLiveData(State())

    var uri: Uri? = null
        private set

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "CreateGroupViewModel onCleared")
    }

    private fun isCreateGroupValid(title: String, description: String) = when {
        TextUtils.isEmpty(title) -> {
            state.value = state.value?.copy(titleError = R.string.require_group_title)
            false
        }
        TextUtils.isEmpty(description) -> {
            state.value = state.value?.copy(descError = R.string.require_group_description)
            false
        }
        else -> true
    }

    private fun createGroup(title: String, description: String, joinType: String, image: String?) {
        repository.addGroup(apiKey, title, description, joinType, image)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value?.copy(
                            isLoading = false,
                            group = result.data
                        )
                    }
                    is Resource.Error -> {
                        state.value = state.value?.copy(
                            isLoading = false,
                            message = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(
                            isLoading = true
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun createGroup() {
        if (isCreateGroupValid(state.value!!.title.value!!, state.value!!.description)) {
            state.value!!.bitmap?.also {
                repository.addGroupImage(apiKey, it)
                    .onEach { result ->
                        when (result) {
                            is Resource.Success -> {
                                val image = result.data

                                createGroup(
                                    title = state.value!!.title.value!!,
                                    description = state.value!!.description,
                                    joinType = if (!state.value!!.joinType) "0" else "1",
                                    image = image
                                )
                            }
                            is Resource.Error -> {
                                state.value = state.value?.copy(
                                    isLoading = false,
                                    message = result.message ?: "An unexpected error occured"
                                )
                            }
                            is Resource.Loading -> {
                                state.value = state.value?.copy(
                                    isLoading = true
                                )
                            }
                        }
                    }
                    .launchIn(viewModelScope)
            } ?: createGroup(
                title = state.value!!.title.value!!,
                description = state.value!!.description,
                joinType = if (!state.value!!.joinType) "0" else "1",
                image = null
            )
        }
    }

    fun onResetClick() {
        state.value = state.value?.copy(title = MutableLiveData(""))
    }

    fun setBitmap(bitmap: Bitmap?) {
        state.value = state.value?.copy(bitmap = bitmap)
    }

    fun setJoinType(boolean: Boolean) {
        state.value = state.value?.copy(joinType = boolean)
    }

    fun getUriToSaveImage(): Uri? {
        uri = photoUriManager.buildNewUri()
        return uri
    }

    init {
        viewModelScope.launch {
            preferenceManager.userFlow
                .collectLatest { user ->
                    apiKey = user?.apiKey ?: ""
                }
        }
    }

    data class State(
        var title: MutableLiveData<String> = MutableLiveData(""),
        var description: String = "",
        val titleError: Int? = null,
        val descError: Int? = null,
        val bitmap: Bitmap? = null,
        val joinType: Boolean = false,
        val isLoading: Boolean = false,
        val group: GroupItem.Group? = null,
        val message: String = ""
    )
}