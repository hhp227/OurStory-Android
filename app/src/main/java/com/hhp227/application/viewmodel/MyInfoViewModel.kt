package com.hhp227.application.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.UserRepository
import com.hhp227.application.dto.Resource
import com.hhp227.application.dto.UserItem
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.IOException

class MyInfoViewModel internal constructor(private val repository: UserRepository, private val preferenceManager: PreferenceManager) : ViewModel() {
    lateinit var user: UserItem

    lateinit var currentPhotoPath: String

    lateinit var photoURI: Uri

    val state = MutableStateFlow(State())

    val userFlow = preferenceManager.userFlow

    val bitmapFlow: MutableStateFlow<Bitmap?> = MutableStateFlow(null)

    private fun updateUserProfile(imageUrl: String = "null") {
        repository.setUserProfile(user.apiKey ?: "", imageUrl).onEach { result ->
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

    suspend fun updateUserDataStore(imageUrl: String) {
        user.copy(profileImage = imageUrl).also { user ->
            this.user = user

            preferenceManager.storeUser(user)
        }
    }

    fun uploadImage() {
        bitmapFlow.value?.let {
            repository.addProfileImage(user.apiKey ?: "", it).onEach { result ->
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

    fun setBitmapFlow(bitmap: Bitmap?) {
        bitmapFlow.value = bitmap
    }

    fun resetState() {
        state.value = State()
    }

    init {
        userFlow.onEach { user ->
            this.user = user ?: UserItem.getDefaultInstance()
        }.launchIn(viewModelScope)
    }

    data class State(
        val isLoading: Boolean = false,
        val imageUrl: String? = null,
        val error: String = ""
    )
}

class MyInfoViewModelFactory(
    private val repository: UserRepository,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyInfoViewModel::class.java)) {
            return MyInfoViewModel(repository, preferenceManager) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}