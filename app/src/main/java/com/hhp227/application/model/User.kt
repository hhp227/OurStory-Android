package com.hhp227.application.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class User(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("email") val email: String? = null,
    @SerialName("api_key") val apiKey: String? = null,
    @SerialName("profile_img") var profileImage: String? = null,
    @SerialName("created_at") val createdAt: String? = null
) : Parcelable {
    companion object {
        fun getDefaultInstance() = User(0, "", null, null, null, null)
    }
}