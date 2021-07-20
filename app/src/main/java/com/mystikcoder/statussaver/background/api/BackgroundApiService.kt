package com.mystikcoder.statussaver.background.api

import com.google.gson.JsonObject
import com.mystikcoder.statussaver.model.mxtakatak.MxTakaTak
import com.mystikcoder.statussaver.model.twitter.TwitterResponse
import retrofit2.Call
import retrofit2.http.*

interface BackgroundApiService {

    @GET
    fun callInstagramDownload(
        @Url value: String,
        @Header("Cookie") cookie: String,
        @Header("User-Agent") userAgent: String
    ): Call<JsonObject>

    @FormUrlEncoded
    @POST
    fun callTwitterDownload(
        @Url url: String,
        @Field("id") id: String
    ): Call<TwitterResponse>

    @GET
    fun getMxTakaTakDownload(
        @Url websiteUrl: String,
        @Query("url") mediaUrl: String
    ) : Call<MxTakaTak>

}