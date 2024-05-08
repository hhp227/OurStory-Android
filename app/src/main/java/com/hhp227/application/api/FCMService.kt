package com.hhp227.application.api

import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.util.InjectorUtils
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PUT
import retrofit2.http.Path

interface FCMService {
    @PUT("user/{user_id}")
    @FormUrlEncoded
    suspend fun sendRegistrationToServer(
        @Path("user_id") userId: Int,
        @Field("fcm_registration_id") token: String,
    ): BasicApiResponse<Unit>

    companion object {
        fun create(): FCMService {
            return InjectorUtils.provideRetrofit()
                .create(FCMService::class.java)
        }
    }
}