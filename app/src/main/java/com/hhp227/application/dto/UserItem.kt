package com.hhp227.application.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserItem(
    var id: Int,
    var name: String,
    var email: String?,
    var apiKey: String,
    var profileImage: String?,
    var createAt: String?
) : Parcelable