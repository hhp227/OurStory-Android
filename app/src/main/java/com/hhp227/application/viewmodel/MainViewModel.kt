package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.hhp227.application.helper.PreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    val user get() = preferenceManager.userFlow.asLiveData()

    var isReady = false
        private set

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "MainViewModel onCleared")
    }

    fun clear() {
        viewModelScope.launch {
            preferenceManager.storeUser(null)
        }
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