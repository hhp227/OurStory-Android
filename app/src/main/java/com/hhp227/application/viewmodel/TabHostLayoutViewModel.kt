package com.hhp227.application.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.dto.GroupItem

class TabHostLayoutViewModel internal constructor(savedStateHandle: SavedStateHandle) : ViewModel() {
    val group = savedStateHandle.get<GroupItem.Group>("group") ?: GroupItem.Group()
}

class TabHostLayoutViewModelFactory(
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(TabHostLayoutViewModel::class.java)) {
            return TabHostLayoutViewModel(handle) as T
        }
        throw IllegalAccessException("Unkown Viewmodel Class")
    }
}