package com.hhp227.application.api

import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.model.GroupItem
import com.hhp227.application.util.InjectorUtils
import okhttp3.MultipartBody
import retrofit2.http.*

interface GroupService {
    @GET("groups")
    suspend fun getNotJoinedGroupList(
        @Header("Authorization") apiKey: String,
        @Query("offset") offset: Int,
        @Query("load_size") loadSize: Int
    ): BasicApiResponse<List<GroupItem.Group>>

    @GET("user_groups")
    suspend fun getMyGroupList(
        @Header("Authorization") apiKey: String,
        @Query("offset") offset: Int,
        @Query("load_size") loadSize: Int,
        @Query("status") status: Int = 0
    ): BasicApiResponse<List<GroupItem.Group>>

    @POST("group_join")
    @FormUrlEncoded
    suspend fun requestJoin(
        @Header("Authorization") apiKey: String,
        @Field("group_id") groupId: Int,
        @Field("status") status: Int
    ): BasicApiResponse<Boolean>

    @POST("group")
    @FormUrlEncoded
    suspend fun addGroup(
        @Header("Authorization") apiKey: String,
        @Field("name") name: String,
        @Field("description") description: String,
        @Field("join_type") joinType: String,
        @Field("image") image: String?
    ): BasicApiResponse<GroupItem.Group>

    @DELETE("group/{group_id}")
    suspend fun removeGroup(
        @Header("Authorization") apiKey: String,
        @Path("group_id") groupId: Int
    ): BasicApiResponse<Unit>

    @DELETE("leave_group/{group_id}")
    suspend fun leaveGroup(
        @Header("Authorization") apiKey: String,
        @Path("group_id") groupId: Int
    ): BasicApiResponse<Unit>

    @Multipart
    @POST("group_image")
    suspend fun uploadImage(
        @Header("Authorization") apiKey: String,
        @Part image: MultipartBody.Part
    ): BasicApiResponse<String>

    companion object {
        fun create(): GroupService {
            return InjectorUtils.provideRetrofit()
                .create(GroupService::class.java)
        }
    }
}