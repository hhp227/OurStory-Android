package com.hhp227.application.model

import java.io.Serializable

data class MessageItem(var id: Int = 0, var message: String? = null, var time: String? = null, var user: User) : Serializable