package com.hhp227.application.api

import com.hhp227.application.model.BasicApiResponse
import com.hhp227.application.model.ListItem
import com.hhp227.application.util.InjectorUtils
import retrofit2.http.*

interface ReplyService {
    @GET("replys/{post_id}")
    suspend fun getReplyList(
        @Header("Authorization") apiKey: String,
        @Path("post_id") postId: Int
    ): BasicApiResponse<List<ListItem.Reply>>

    @GET("replys/post/{reply_id}")
    suspend fun getReply(
        @Header("Authorization") apiKey: String,
        @Path("reply_id") replyId: Int
    ): BasicApiResponse<ListItem.Reply>

    @POST("replys/{post_id}")
    @FormUrlEncoded
    suspend fun addReply(
        @Header("Authorization") apiKey: String,
        @Path("post_id") portId: Int,
        @Field("reply") text: String
    ): BasicApiResponse<ListItem.Reply>

    /*@PUT("replys/post/{reply_id}")
    @FormUrlEncoded
    suspend fun setReply(
        @Header("Authorization") apiKey: String,
        @Path("reply_id") replyId: Int,
        @Field("reply") text: String,
        @Field("status") status: String = "0"
    ): BasicApiResponse<String>*/
    @POST("replys/post/{reply_id}")
    @FormUrlEncoded
    suspend fun setReply(
        @Header("Authorization") apiKey: String,
        @Path("reply_id") replyId: Int,
        @Field("reply") text: String,
        @Field("status") status: String = "0",
        @Field("_METHOD") method: String = "PUT"
    ): BasicApiResponse<String>

    /*@DELETE("replys/post/{reply_id}")
    suspend fun removeReply(
        @Header("Authorization") apiKey: String,
        @Path("reply_id") replyId: Int
    ): BasicApiResponse<Boolean>*/
    @POST("replys/post/{reply_id}")
    @FormUrlEncoded
    suspend fun removeReply(
        @Header("Authorization") apiKey: String,
        @Path("reply_id") replyId: Int,
        @Field("_METHOD") method: String = "DELETE"
    ): BasicApiResponse<Boolean>

    companion object {
        fun create(): ReplyService {
            return InjectorUtils.provideRetrofit()
                .create(ReplyService::class.java)
        }
    }
}