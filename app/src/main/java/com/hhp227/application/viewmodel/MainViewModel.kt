package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

class MainViewModel : ViewModel() {
    val itemList: MutableList<Any> by lazy { arrayListOf() }

    val state = MutableStateFlow(State())

    fun getList() = callbackFlow<Any> {  }

    fun temp() {
        getList().onCompletion { cause ->
            when (cause) {
                //state.value.hasRequestedMore = false
            }
        }.onEach {

        }
    }

    fun onReceive() {

    }

    data class State(
        val itemList: MutableList<*> = mutableListOf<Any>(),
        var offset: Int = 0,
        var hasRequestedMore: Boolean = false,
        var isLoading: Boolean = false,
        var error: String = ""
    )
}