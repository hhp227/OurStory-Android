package com.hhp227.application.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO 후에 api를 이 양식으로 맞추기
@Serializable
data class BasicApiResponse<T>(
    @SerialName("error") val error: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("result") val data: T? = null
)
