package com.mystikcoder.statussaver.core.data.repository.tiktok.implementation

import com.mystikcoder.statussaver.core.data.networking.ApiService
import com.mystikcoder.statussaver.core.data.repository.tiktok.abstraction.TiktokDownloadRepository
import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse
import com.mystikcoder.statussaver.framework.utils.Utils
import javax.inject.Inject

class TiktokDownloadRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : TiktokDownloadRepository {

    override suspend fun downloadTiktokFile(url: String): DownloadRequestResponse {
        val response = apiService.getMxTakaTak(Utils.MX_TAKA_TAK_URL, url)
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