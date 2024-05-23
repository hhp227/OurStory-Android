package com.hhp227.application.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BasicApiResponse<T>(
    @SerialName("error") val error: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("result") val data: T? = null
)
