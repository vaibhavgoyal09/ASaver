package com.mystikcoder.statussaver.domain.repository.mxtakatak.implementation

import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse
import com.mystikcoder.statussaver.domain.networking.ApiService
import com.mystikcoder.statussaver.domain.repository.mxtakatak.abstraction.MxTakaTakDownloadRepository
import com.mystikcoder.statussaver.presentation.utils.Utils
import javax.inject.Inject

class MxTakaTakDownloadRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : MxTakaTakDownloadRepository {

    override suspend fun downloadMxTakaTakFile(mediaUrl: String): DownloadRequestResponse {
        val response = apiService.getMxTakaTak(Utils.MX_TAKA_TAK_URL, mediaUrl)
        val result = response.body()

        kotlin.runCatching {
            return if (response.isSuccessful && result != null) {

                if (result.responseCode == "200") {
                    val mainVideo = result.data.mainVideo
                    DownloadRequestResponse(isSuccess = true, downloadLink = mainVideo)
                } else {
                    DownloadRequestResponse(errorMessage = "No data found")
                }

            } else {
                DownloadRequestResponse(errorMessage = "No data found")
            }
        }.getOrElse {
            return DownloadRequestResponse(errorMessage = it.message ?: "Something Went Wrong")
        }
    }
}