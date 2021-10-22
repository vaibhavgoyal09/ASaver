package com.mystikcoder.statussaver.core.data.repository.josh.implementation

import com.mystikcoder.statussaver.core.data.repository.josh.abstraction.JoshDownloadRepository
import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse
import org.json.JSONObject
import org.jsoup.Jsoup

class JoshDownloadRepositoryImpl : JoshDownloadRepository {

    override suspend fun downloadJoshFile(url: String): DownloadRequestResponse {

        kotlin.runCatching {
            val document = Jsoup.connect(url).get()

            val html =
                document.select("script[id=\"__NEXT_DATA__\"]")
                    .last()
                    ?.html()

            return if (!html.isNullOrEmpty()) {

                val videoUrl =
                    (JSONObject(html).getJSONObject("props").getJSONObject("pageProps")
                        .getJSONObject("detail").getJSONObject("data")
                        .getString("m3u8_url")).toString()

                DownloadRequestResponse(isSuccess = true , downloadLink = videoUrl)
            } else{
                DownloadRequestResponse(errorMessage = "No data found")
            }
        }.getOrElse {
            return DownloadRequestResponse(errorMessage = it.message ?: "Something Went Wrong")
        }
    }
}