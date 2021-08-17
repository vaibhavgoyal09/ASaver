package com.mystikcoder.statussaver.data.networking

import com.mystikcoder.statussaver.domain.model.util.FacebookBody
import org.json.JSONObject
import org.jsoup.helper.HttpConnection
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface FacebookApiService {

    @POST
    @Headers(
        "accept-language: en,en-US;q=0.9,fr;q=0.8,ar;q=0.7",
        "${HttpConnection.CONTENT_TYPE}: application/json"
    )
    suspend fun getUsers(
        @Header("cookie") fbCookies: String,
        @Header("user-agent") userAgent: String,
        @Body body: FacebookBody
    ): Response<JSONObject>

    @POST
    @Headers(
        "accept-language: en,en-US;q=0.9,fr;q=0.8,ar;q=0.7",
        "${HttpConnection.CONTENT_TYPE}: application/json"
    )
    suspend fun getUserStories(
        @Header("cookies") cookies: String,
        @Header("user-agent") userAgent: String,
        @Body body: FacebookBody
    ): Response<JSONObject>
}