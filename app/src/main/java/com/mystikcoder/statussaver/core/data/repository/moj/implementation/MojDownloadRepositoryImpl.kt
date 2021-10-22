package com.mystikcoder.statussaver.core.data.repository.moj.implementation

import com.mystikcoder.statussaver.core.data.networking.ApiService
import com.mystikcoder.statussaver.core.data.repository.moj.abstraction.MojDownloadRepository
import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse
import com.mystikcoder.statussaver.framework.utils.Utils
import javax.inject.Inject

class MojDownloadRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : MojDownloadRepository {

    override suspend fun downloadMojFile(url: String): DownloadRequestResponse {
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