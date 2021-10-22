package com.mystikcoder.statussaver.core.data.repository.twitter.implementation

import com.mystikcoder.statussaver.core.data.networking.ApiService
import com.mystikcoder.statussaver.core.data.repository.twitter.abstraction.TwitterDownloadRepository
import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse
import com.mystikcoder.statussaver.core.domain.model.twitter.TwitterResponse
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class TwitterDownloadRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : TwitterDownloadRepository {

    override suspend fun downloadTwitterFile(
        url: String
    ): DownloadRequestResponse {

        val response: Response<TwitterResponse> = apiService.callTwitter(
            "https://twittervideodownloaderpro.com/twittervideodownloadv2/index.php",
            url
        )
        val result = response.body()

        kotlin.runCatching {
            return if (response.isSuccessful && result != null) {

                var fileUrl = result.videos[0].url
                val mimeType = result.videos[0].type

                if (mimeType == "image") {
                    DownloadRequestResponse(isSuccess = true, downloadLink = fileUrl)
                } else {
                    fileUrl = result.videos[result.videos.size - 1].url
                    DownloadRequestResponse(isSuccess = true, downloadLink = fileUrl)
                }

            } else {
                DownloadRequestResponse(errorMessage = "No data found")
            }
        }.getOrElse {
            Timber.e(it)
            return DownloadRequestResponse(errorMessage = it.message ?: "Something Went Wrong")
        }
    }
}