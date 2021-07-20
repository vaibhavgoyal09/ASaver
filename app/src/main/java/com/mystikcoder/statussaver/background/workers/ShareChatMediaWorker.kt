package com.mystikcoder.statussaver.background.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mystikcoder.statussaver.utils.WORKER_KEY_DOWNLOAD_URL
import com.mystikcoder.statussaver.utils.WORK_PLATFORM_NAME
import com.mystikcoder.statussaver.utils.WORK_URL
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.regex.Matcher
import java.util.regex.Pattern

@HiltWorker
class ShareChatMediaWorker @AssistedInject constructor
    (
    @Assisted val context: Context,
    @Assisted workerParameters: WorkerParameters
) :
    Worker(context, workerParameters) {

    override fun doWork(): Result {

        Log.e("Workers", "Share chat worker called")

        val url = inputData.getString(WORK_URL)
        val platform = inputData.getString(WORK_PLATFORM_NAME)

        var videoUrl: String?

        if (url != null && platform != null) {

            try {
                val document: Document? = Jsoup.connect(url).get()
                document?.let {
                    when (platform) {
                        "sharechat" -> {
                            videoUrl =
                                document.select("meta[property=\"og:video:secure_url\"]")
                                    ?.last()
                                    ?.attr("content")

                        }
                        "facebook" -> {
                            videoUrl = document.select("meta[property=\"og:video:url\"]")
                                ?.last()
                                ?.attr("content")
                        }
                        "mitron" -> {
                            val html =
                                document.select("script[id=\"__NEXT_DATA__\"]")
                                    ?.last()
                                    ?.html()

                            if (!html.isNullOrEmpty()) {
                                videoUrl =
                                    (JSONObject(html).getJSONObject("props")
                                        .getJSONObject("pageProps")
                                        .getJSONObject("video").getString("videoUrl")).toString()
                            } else {
                                return Result.failure()
                            }
                        }
                        "roposo" -> {
                            videoUrl = document.select("meta[property=\"og:video\"]")
                                ?.last()
                                ?.attr("content")
                        }
                        "chingari" -> {
                            videoUrl = document.select("meta[property=\"og:video:secure_url\"]")
                                ?.last()
                                ?.attr("content")
                        }
                        "likee" -> {

                            val pattern =
                                Pattern.compile("window\\.data \\s*=\\s*(\\{.+?\\});") // Do not remove back slashes

                            var jsonData = ""
                            val matcher: Matcher = pattern.matcher(document.toString())
                            while (matcher.find()) {
                                jsonData =
                                    matcher.group().replaceFirst("window.data = ".toRegex(), "")
                                        .replace(";", "")
                            }

                            val jsonObject = JSONObject(jsonData)
                            videoUrl = jsonObject.getString("video_url").replace("_4", "")
                        }
                        else -> return Result.failure()
                    }
                    if (videoUrl != null) {
                        val data = Data.Builder().putString(WORKER_KEY_DOWNLOAD_URL, videoUrl)
                        Result.success(data.build())
                    } else if (videoUrl == null && platform == "sharechat") {
                        videoUrl =
                            document.select("meta[property=\"og:image\"]")
                                ?.last()
                                ?.attr("content")
                    } else {
                        return Result.failure()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return Result.failure()
    }
}
