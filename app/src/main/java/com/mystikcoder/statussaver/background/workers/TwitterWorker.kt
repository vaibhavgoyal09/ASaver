package com.mystikcoder.statussaver.background.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mystikcoder.statussaver.background.repository.BackgroundFetcher
import com.mystikcoder.statussaver.utils.Utils
import com.mystikcoder.statussaver.utils.WORKER_KEY_DOWNLOAD_URL
import com.mystikcoder.statussaver.utils.WORK_URL
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TwitterWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workParams: WorkerParameters,
    private val fetcher: BackgroundFetcher
) : Worker(context, workParams) {

    override fun doWork(): Result {
        Log.e("Worker", "Twitter do worker called")
        val url = inputData.getString(WORK_URL)

        if (url != null) {
            try {
                fetcher.downloadTwitterMedia(url)
                    .execute()
                    .body()?.let {
                        val videoType = it.videos[0].type

                        val mediaUrl =
                            if (videoType == "image") it.videos[0].url else it.videos[it.videos.size - 1].url

                        val data = Data.Builder().putString(WORKER_KEY_DOWNLOAD_URL, mediaUrl)
                        return Result.success(data.build())
                    }
            }catch (e: Exception){
                e.printStackTrace()
                Utils.createToast(context, "No media found on this tweet")
            }
        }
        return Result.failure()
    }
}
