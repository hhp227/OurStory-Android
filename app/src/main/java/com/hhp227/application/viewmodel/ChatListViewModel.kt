package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import com.hhp227.application.dto.ChatRoomItem

class ChatListViewModel : ViewModel() {
    val chatRooms by lazy { mutableListOf<ChatRoomItem>() }
}