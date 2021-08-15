package com.mystikcoder.statussaver.domain.repository.chingari.implementation

import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse
import com.mystikcoder.statussaver.domain.repository.chingari.abstraction.ChingariDownloadRepository
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class ChingariDownloadRepositoryImpl : ChingariDownloadRepository {

    override suspend fun downloadChingariFile(url: String): DownloadRequestResponse {

        kotlin.runCatching {

            val document: Document = Jsoup.connect(url).get()

            val videoUrl =
                document.select("meta[property=\"og:video:secure_url\"]")
                    .last()
                    .attr("content")
            return if (!videoUrl.isNullOrEmpty()) {
                DownloadRequestResponse(isSuccess = true, downloadLink = videoUrl)
            } else {
                DownloadRequestResponse(errorMessage = "No data found")
            }
        }.getOrElse {
            return DownloadRequestResponse(errorMessage = it.message ?: "Something Went Wrong")
        }
    }
}