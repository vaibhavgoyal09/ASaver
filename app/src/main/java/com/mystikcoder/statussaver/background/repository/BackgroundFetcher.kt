package com.mystikcoder.statussaver.background.repository

import com.google.gson.JsonObject
import com.mystikcoder.statussaver.background.api.BackgroundApiService
import com.mystikcoder.statussaver.model.mxtakatak.MxTakaTak
import com.mystikcoder.statussaver.model.twitter.TwitterResponse
import com.mystikcoder.statussaver.utils.Utils
import retrofit2.Call
import javax.inject.Inject

class BackgroundFetcher @Inject constructor(
    private val apiService: BackgroundApiService
) {

    fun downloadInstagramMedia(url: String, cookie: String, userAgent: String): Call<JsonObject> {
        return apiService.callInstagramDownload(url, cookie, userAgent)
    }

    fun downloadMxTakaTakMedia(url: String): Call<MxTakaTak> {
        return apiService.getMxTakaTakDownload(Utils.MX_TAKA_TAK_URL, url)
    }

    fun downloadTwitterMedia(id: String): Call<TwitterResponse> {
        return apiService.callTwitterDownload(
            "https://twittervideodownloaderpro.com/twittervideodownloadv2/index.php",
            id
        )
    }
}