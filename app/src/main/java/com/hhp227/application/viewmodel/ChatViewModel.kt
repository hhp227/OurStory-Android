package com.hhp227.application.viewmodel

import androidx.lifecycle.ViewModel
import com.hhp227.application.dto.MessageItem

class ChatViewModel : ViewModel() {
    val listMessages by lazy { arrayListOf<MessageItem>() }

    var chatRoomId: Int = 0
}