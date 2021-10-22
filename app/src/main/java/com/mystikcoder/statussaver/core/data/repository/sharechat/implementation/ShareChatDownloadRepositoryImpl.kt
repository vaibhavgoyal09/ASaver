package com.mystikcoder.statussaver.core.data.repository.sharechat.implementation

import com.mystikcoder.statussaver.core.data.repository.sharechat.abstraction.ShareChatDownloadRepository
import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse
import org.jsoup.Jsoup

class ShareChatDownloadRepositoryImpl: ShareChatDownloadRepository {

    override suspend fun downloadShareChatFile(url: String): DownloadRequestResponse {

        kotlin.runCatching {
            val document = Jsoup.connect(url).get()

            val videoUrl = document.select("meta[property=\"og:video:secure_url\"]")
                .last()
                ?.attr("content")

            return if (videoUrl != null){
                DownloadRequestResponse(isSuccess = true , downloadLink = videoUrl)
            }else{
                val imageUrl = document.select("meta[property=\"og:image\"]")
                    .last()
                    ?.attr("content")

                if (imageUrl != null){
                    DownloadRequestResponse(isSuccess = true , downloadLink = imageUrl)
                } else{
                    DownloadRequestResponse(errorMessage = "No data found")
                }
            }
        }.getOrElse {
            return DownloadRequestResponse(errorMessage = it.message ?: "Something Went Wrong")
        }
    }
}