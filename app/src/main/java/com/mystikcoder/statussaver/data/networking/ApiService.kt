package com.mystikcoder.statussaver.data.networking

import com.google.gson.JsonObject
import com.mystikcoder.statussaver.domain.model.instagram.FullDetailModel
import com.mystikcoder.statussaver.domain.model.instagram.StoryModel
import com.mystikcoder.statussaver.domain.model.mxtakatak.MxTakaTak
import com.mystikcoder.statussaver.domain.model.twitter.TwitterResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET
    suspend fun callResult(
        @Url value: String,
        @Header("Cookie") cookie: String,
        @Header("User-Agent") userAgent: String
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST
    suspend fun callTwitter(
        @Url url: String,
        @Field("id") id: String
    ): Response<TwitterResponse>

    @GET
    suspend fun getStoriesApi(
        @Url value: String,
        @Header("Cookie") cookie: String,
        @Header("User-Agent") userAgent: String
    ): Response<StoryModel>

    @GET
    suspend fun getFullDetailInfoApi(
        @Url value: String,
        @Header("Cookie") cookie: String,
        @Header("User-Agent") userAgent: String
    ): Response<FullDetailModel>

    @GET
    suspend fun getMxTakaTak(
        @Url websiteUrl: String,
        @Query("url") mediaUrl: String
    ) : Response<MxTakaTak>
}