package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hhp227.application.helper.PreferenceManager

class SplashViewModel(preferenceManager: PreferenceManager) : ViewModel() {
    val userFlow = preferenceManager.userFlow
}

class SplashViewModelFactory(
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            return SplashViewModel(preferenceManager) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}