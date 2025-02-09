package com.hhp227.application.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreference(val user: User?, val notifications: String? = null)