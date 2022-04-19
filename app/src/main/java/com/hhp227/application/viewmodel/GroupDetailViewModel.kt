package com.hhp227.application.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.hhp227.application.dto.GroupItem

class GroupDetailViewModel internal constructor(savedStateHandle: SavedStateHandle) : ViewModel() {
    val group = savedStateHandle.get<GroupItem.Group>("group") ?: GroupItem.Group()
}

class GroupDetailViewModelFactory(
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(GroupDetailViewModel::class.java)) {
            return GroupDetailViewModel(handle) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}