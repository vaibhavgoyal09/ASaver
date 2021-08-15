package com.mystikcoder.statussaver.domain.repository.roposo.implementation

import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse
import com.mystikcoder.statussaver.domain.repository.roposo.abstraction.RoposoDownloadRepository
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class RoposeDownloadRepositoryImpl : RoposoDownloadRepository {

    override suspend fun downloadRoposeFile(url: String): DownloadRequestResponse {

        kotlin.runCatching {

            val document: Document = Jsoup.connect(url).get()

            var videoUrl =
                document.select("meta[property=\"og:video\"]")
                    .last()
                    .attr("content")

            if (videoUrl.isNullOrEmpty()) {
                videoUrl =
                    document.select("meta[property=\"og:video:url\"]")
                        .last()
                        .attr("content")

                return if (!videoUrl.isNullOrEmpty()) {
                    DownloadRequestResponse(isSuccess = true , downloadLink = videoUrl)
                }else{
                    DownloadRequestResponse(errorMessage = "No data found")
                }
            }else{
                return DownloadRequestResponse(errorMessage = "No data found")
            }
        }.getOrElse {
            return DownloadRequestResponse(errorMessage = it.message ?: "Something Went Wrong")
        }
    }
}