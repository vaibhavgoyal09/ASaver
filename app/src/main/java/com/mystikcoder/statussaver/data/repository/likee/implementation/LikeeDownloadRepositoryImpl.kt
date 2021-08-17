package com.mystikcoder.statussaver.data.repository.likee.implementation

import com.mystikcoder.statussaver.data.repository.likee.abstraction.LikeeDownloadRepository
import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.regex.Matcher
import java.util.regex.Pattern

class LikeeDownloadRepositoryImpl: LikeeDownloadRepository {

    override suspend fun downloadLikeeFile(url: String): DownloadRequestResponse {

        val pattern =
            Pattern.compile("window\\.data \\s*=\\s*(\\{.+?\\});")

        kotlin.runCatching {
            val document: Document = Jsoup.connect(url).get()

            var jsonData = ""
            val matcher: Matcher = pattern.matcher(document.toString())
            while (matcher.find()) {
                jsonData = matcher.group().replaceFirst("window.data = ".toRegex(), "")
                    .replace(";", "")
            }

            val jsonObject = JSONObject(jsonData)
            val videoUrl = jsonObject.getString("video_url").replace("_4", "")

           return DownloadRequestResponse(isSuccess = true , downloadLink = videoUrl)
        }.getOrElse {
            return DownloadRequestResponse(isSuccess = true , errorMessage = it.message ?: "Something Went Wrong")
        }
    }
}