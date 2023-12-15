package com.hhp227.application.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.hhp227.application.data.UserRepository
import com.hhp227.application.model.Resource
import com.hhp227.application.model.User
import com.hhp227.application.helper.PhotoUriManager
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MyInfoViewModel internal constructor(
    private val repository: UserRepository,
    private val preferenceManager: PreferenceManager,
    private val photoUriManager: PhotoUriManager
) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableLiveData(State())

    var photoURI: Uri? = null
        private set

    private fun updateUserProfile(imageUrl: String = "null") {
        repository.setUserProfile(apiKey, imageUrl)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state.value = state.value?.copy(
                            isLoading = false,
                            userInfo = state.value?.userInfo?.copy(profileImage = result.data)
                        )
                    }
                    is Resource.Error -> {
                        state.value = state.value?.copy(
                            isLoading = false,
                            error = result.message ?: "An unexpected error occured"
                        )
                    }
                    is Resource.Loading -> {
                        state.value = state.value?.copy(isLoading = true)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateUserDataStore(user: User) {
        viewModelScope.launch {
            preferenceManager.storeUser(user)
        }
    }

    fun uploadImage() {
        state.value?.bitmap?.let {
            repository.addProfileImage(apiKey, it)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            val imageUrl = result.data ?: "null"

                            updateUserProfile(imageUrl)
                        }
                        is Resource.Error -> {
                            state.value = state.value?.copy(
                                isLoading = false,
                                error = result.message ?: "An unexpected error occured"
                            )
                        }
                        is Resource.Loading -> {
                            state.value = state.value?.copy(isLoading = true)
                        }
                    }
                }
                .launchIn(viewModelScope)
        } ?: updateUserProfile()
    }

    fun setBitmap(bitmap: Bitmap?) {
        state.value = state.value?.copy(bitmap = bitmap)
    }

    fun resetState() {
        state.value = State()
    }

    fun getUriToSaveImage(): Uri? {
        photoURI = photoUriManager.buildNewUri()
        return photoURI
    }

    init {
        preferenceManager.userFlow
            .onEach { user ->
                apiKey = user?.apiKey ?: ""

                state.postValue(state.value?.copy(userInfo = user ?: User.getDefaultInstance()))
            }
            .launchIn(viewModelScope)
    }

    data class State(
        val isLoading: Boolean = false,
        val userInfo: User? = null,
        val bitmap: Bitmap? = null,
        val error: String = ""
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