package com.hhp227.application.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.R
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.dto.Resource
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CreateGroupViewModel internal constructor(private val repository: GroupRepository, preferenceManager: PreferenceManager) : ViewModel() {
    private lateinit var apiKey: String

    lateinit var uri: Uri

    lateinit var currentPhotoPath: String

    val state = MutableStateFlow(State())

    val bitmapFlow: MutableStateFlow<Bitmap?> = MutableStateFlow(null)

    var joinType = false

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "CreateGroupViewModel onCleared")
    }

    private fun isCreateGroupValid(title: String, description: String) = when {
        TextUtils.isEmpty(title) -> {
            state.value = state.value.copy(
                createGroupFormState = CreateGroupFormState(titleError = R.string.require_group_title)
            )
            false
        }
        TextUtils.isEmpty(description) -> {
            state.value = state.value.copy(
                createGroupFormState = CreateGroupFormState(descError = R.string.require_group_description)
            )
            false
        }
        else -> true
    }

    private fun createGroup(title: String, description: String, joinType: String, image: String?) {
        repository.addGroup(apiKey, title, description, joinType, image)
            .onEach { result ->
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
            }
            .launchIn(viewModelScope)
    }

    fun setBitmap(bitmap: Bitmap?) {
        bitmapFlow.value = bitmap
    }

    fun createGroup(title: String, description: String, joinType: String) {
        if (isCreateGroupValid(title, description)) {
            bitmapFlow.value?.also {
                repository.addGroupImage(apiKey, it)
                    .onEach { result ->
                        when (result) {
                            is Resource.Success -> {
                                val image = result.data

                                createGroup(title, description, joinType, image)
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
                    }
                    .launchIn(viewModelScope)
            } ?: createGroup(title, description, joinType, null)
        }
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
        val isLoading: Boolean = false,
        val group: GroupItem.Group? = null,
        val createGroupFormState: CreateGroupFormState? = null,
        val error: String = ""
    )

    data class CreateGroupFormState(
        val titleError: Int? = null,
        val descError: Int? = null
    )
}

class CreateGroupViewModelFactory(
    private val repository: GroupRepository,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateGroupViewModel::class.java)) {
            return CreateGroupViewModel(repository, preferenceManager) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}