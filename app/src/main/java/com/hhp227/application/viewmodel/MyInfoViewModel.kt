package com.hhp227.application.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.UserRepository
import com.hhp227.application.dto.Resource
import com.hhp227.application.dto.UserItem
import com.hhp227.application.helper.PhotoUriManager
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MyInfoViewModel internal constructor(
    private val repository: UserRepository,
    private val preferenceManager: PreferenceManager,
    private val photoUriManager: PhotoUriManager
) : ViewModel() {
    private lateinit var currentUserInfo: UserItem

    val state = MutableStateFlow(State())

    val userFlow = preferenceManager.userFlow

    val imageHolder: MutableStateFlow<ProfileImageHolder> = MutableStateFlow(ProfileImageHolder(null, null))

    var photoURI: Uri? = null
        private set

    private fun updateUserProfile(imageUrl: String = "null") {
        repository.setUserProfile(currentUserInfo.apiKey ?: "", imageUrl)
            .onEach { result ->
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
            }
            .launchIn(viewModelScope)
    }

    suspend fun updateUserDataStore(imageUrl: String) {
        currentUserInfo.copy(profileImage = imageUrl).also { user ->
            this.currentUserInfo = user

            preferenceManager.storeUser(user)
        }
    }

    fun uploadImage() {
        imageHolder.value.bitmap?.let {
            repository.addProfileImage(currentUserInfo.apiKey ?: "", it)
                .onEach { result ->
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
                }
                .launchIn(viewModelScope)
        } ?: updateUserProfile()
    }

    fun setBitmap(bitmap: Bitmap?) {
        imageHolder.value = imageHolder.value.copy(bitmap = bitmap, imageUrl = null)
    }

    fun resetState() {
        state.value = State()
    }

    fun getUriToSaveImage(): Uri? {
        photoURI = photoUriManager.buildNewUri()
        return photoURI
    }

    init {
        userFlow.onEach { user ->
            currentUserInfo = user ?: UserItem.getDefaultInstance()
            imageHolder.value = ProfileImageHolder(null, user?.profileImage)
        }
            .launchIn(viewModelScope)
    }

    data class State(
        val isLoading: Boolean = false,
        val imageUrl: String? = null,
        val error: String = ""
    )

    data class ProfileImageHolder(
        var bitmap: Bitmap?,
        var imageUrl: String?
    )
}

class MyInfoViewModelFactory(
    private val repository: UserRepository,
    private val preferenceManager: PreferenceManager,
    private val photoUriManager: PhotoUriManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyInfoViewModel::class.java)) {
            return MyInfoViewModel(repository, preferenceManager, photoUriManager) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}