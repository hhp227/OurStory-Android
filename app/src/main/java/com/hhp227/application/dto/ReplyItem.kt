package com.hhp227.application.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReplyItem(
    var id: Int = 0,
    var userId: Int = 0,
    var name: String? = null,
    var profileImage: String? = null,
    var timeStamp: String? = null,
    var reply: String? = null
) : Parcelable