package com.hhp227.application.dto

import java.io.Serializable

data class User(
    var id: Int,
    var name: String,
    var email: String?,
    var apiKey: String?,
    var profileImage: String?,
    var createAt: String?
) : Serializable