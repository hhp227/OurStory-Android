package com.hhp227.application.api

import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.model.ListItem
import com.hhp227.application.util.InjectorUtils
import okhttp3.MultipartBody
import retrofit2.http.*

interface PostService {
    @GET("posts")
    suspend fun getPostList(
        @Query("group_id") groupId: Int,
        @Query("page") page: Int,
        @Query("load_size") loadSize: Int
    ): BasicApiResponse<List<ListItem.Post>>

    @GET("posts_image")
    suspend fun getPostListWithImage(
        @Query("group_id") groupId: Int,
        @Query("page") page: Int,
        @Query("load_size") loadSize: Int
    ): BasicApiResponse<List<ListItem.Post>>

    @GET("posts/")
    suspend fun getUserPostList(
        @Header("Authorization") apiKey: String,
        @Query("page") page: Int,
        @Query("load_size") loadSize: Int
    ): BasicApiResponse<List<ListItem.Post>>

    @GET("post/{post_id}")
    suspend fun getPost(
        @Path("post_id") postId: Int
    ): BasicApiResponse<ListItem.Post>

    @POST("post")
    @FormUrlEncoded
    suspend fun addPost(
        @Header("Authorization") apiKey: String,
        @Field("text") text: String,
        @Field("group_id") groupId: Int
    ): BasicApiResponse<Int>

    @PUT("post/{post_id}")
    @FormUrlEncoded
    suspend fun setPost(
        @Header("Authorization") apiKey: String,
        @Path("post_id") postId: Int,
        @Field("text") text: String,
        @Field("status") status: Int = 0
    ): BasicApiResponse<Int>

    @DELETE("post/{post_id}")
    suspend fun removePost(
        @Header("Authorization") apiKey: String,
        @Path("post_id") postId: Int
    ): BasicApiResponse<Unit>

    @Multipart
    @POST("image")
    suspend fun uploadImage(
        @Header("Authorization") apiKey: String,
        @Part("post_id") postId: Int,
        @Part image: MultipartBody.Part
    ): BasicApiResponse<String>

    @POST("images")
    @FormUrlEncoded
    suspend fun removeImages(
        @Header("Authorization") apiKey: String,
        @Field("post_id") postId: Int,
        @Field("ids") imageIds: String
    ): BasicApiResponse<String>

    @GET("like/{post_id}")
    suspend fun togglePostLike(
        @Header("Authorization") apiKey: String,
        @Path("post_id") postId: Int
    ): BasicApiResponse<String>

    @GET("report/{post_id}")
    suspend fun toggleReport(
        @Header("Authorization") apiKey: String,
        @Path("post_id") postId: Int
    ): BasicApiResponse<String>

    companion object {
        fun create(): PostService {
            return InjectorUtils.provideRetrofit()
                .create(PostService::class.java)
        }
    }
}