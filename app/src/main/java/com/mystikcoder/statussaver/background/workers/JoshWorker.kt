package com.mystikcoder.statussaver.background.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mystikcoder.statussaver.utils.WORKER_KEY_DOWNLOAD_URL
import com.mystikcoder.statussaver.utils.WORK_URL
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@HiltWorker
class JoshWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {

        val link = inputData.getString(WORK_URL)

        if (link != null) {
            val document: Document? = Jsoup.connect(link).get()
            val html =
                document?.select("script[id=\"__NEXT_DATA__\"]")
                    ?.last()
                    ?.html()

            if (!html.isNullOrEmpty()) {

                val videoUrl =
                    (JSONObject(html).getJSONObject("props").getJSONObject("pageProps")
                        .getJSONObject("detail").getJSONObject("data")
                        .getString("m3u8_url")).toString()
                return Result.success(Data.Builder().putString(WORKER_KEY_DOWNLOAD_URL, videoUrl).build())
            }
        }
        return Result.failure()
    }
}