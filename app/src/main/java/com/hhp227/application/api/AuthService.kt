package com.hhp227.application.api

import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.model.User
import com.hhp227.application.util.InjectorUtils
import kotlinx.serialization.json.Json
import retrofit2.http.*

interface AuthService {
    @POST("login")
    @FormUrlEncoded
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): BasicApiResponse<User>

    @POST("register")
    @FormUrlEncoded
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): BasicApiResponse<Unit>

    @PUT("user/{user_id}")
    @FormUrlEncoded
    suspend fun sendRegistrationToServer(
        @Path("user_id") userId: Int,
        @Field("fcm_registration_id") token: String,
    ): BasicApiResponse<Unit>

    companion object {
        fun create(): AuthService {
            return InjectorUtils.provideRetrofit()
                .create(AuthService::class.java)
        }
    }
}