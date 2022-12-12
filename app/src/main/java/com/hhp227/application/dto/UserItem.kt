package com.hhp227.application.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class UserItem(
    @SerialName("id") var id: Int,
    @SerialName("name") var name: String,
    @SerialName("email") var email: String?,
    @SerialName("api_key") var apiKey: String?,
    @SerialName("profile_img") var profileImage: String?,
    @SerialName("created_at") var createdAt: String?
) : Parcelable {
    companion object {
        fun getDefaultInstance() = UserItem(0, "", null, null, null, null)
    }
}