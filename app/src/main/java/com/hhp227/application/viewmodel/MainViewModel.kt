package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(preferenceManager: PreferenceManager) : ViewModel() {
    val userFlow = preferenceManager.userFlow

    var isReady = false
        private set

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "MainViewModel onCleared")
    }

    init {
        viewModelScope.launch {
            delay(SPLASH_TIME_OUT)
            isReady = true
        }
    }

    companion object {
        private const val SPLASH_TIME_OUT = 1250L
    }
}

class MainViewModelFactory(
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(preferenceManager) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}