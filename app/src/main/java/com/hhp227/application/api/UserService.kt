package com.hhp227.application.api

import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.model.User
import com.hhp227.application.util.InjectorUtils
import okhttp3.MultipartBody
import retrofit2.http.*

interface UserService {

    @GET("users")
    suspend fun getUserList(
        @Query("group_id") groupId: Int,
        @Query("offset") offset: Int,
        @Query("load_size") loadSize: Int
    ): BasicApiResponse<List<User>>

    @Multipart
    @POST("profile_img")
    suspend fun uploadImage(
        @Header("Authorization") apiKey: String,
        @Part image: MultipartBody.Part
    ): BasicApiResponse<String>

    @PUT("profile")
    @FormUrlEncoded
    suspend fun setProfile(
        @Header("Authorization") apiKey: String,
        @Field("profile_img") imageUrl: String?,
        @Field("status") status: Int
    ): BasicApiResponse<Unit>

    @GET("friends")
    suspend fun getFriendList(
        @Header("Authorization") apiKey: String,
        @Query("offset") offset: Int
    ): BasicApiResponse<List<User>>

    @GET("friend/{user_id}")
    suspend fun isFriend(
        @Header("Authorization") apiKey: String,
        @Path("user_id") userId: Int
    ): BasicApiResponse<Map<String, Int>>

    @GET("toggle_friend/{user_id}")
    suspend fun toggleFriend(
        @Header("Authorization") apiKey: String,
        @Path("user_id") userId: Int
    ): BasicApiResponse<String>

    companion object {
        fun create(): UserService {
            return InjectorUtils.provideRetrofit()
                .create(UserService::class.java)
        }
    }
}