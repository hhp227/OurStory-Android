package com.hhp227.application.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.hhp227.application.model.ListItem

class PictureViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val state = MutableLiveData(
        State(
            position = savedStateHandle.get<Int>("position") ?: -1,
            list = savedStateHandle.get<Array<ListItem.Image>>("images")?.toList() ?: emptyList()
        )
    )

    fun setPosition(position: Int) {
        state.value = state.value?.copy(position = position)
    }

    data class State(
        val position: Int = -1,
        val list: List<ListItem.Image> = emptyList()
    )
}