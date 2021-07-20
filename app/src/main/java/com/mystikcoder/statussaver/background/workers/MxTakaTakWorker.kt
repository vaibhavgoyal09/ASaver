package com.mystikcoder.statussaver.background.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mystikcoder.statussaver.background.repository.BackgroundFetcher
import com.mystikcoder.statussaver.utils.WORKER_KEY_DOWNLOAD_URL
import com.mystikcoder.statussaver.utils.WORK_URL
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MxTakaTakWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workParams: WorkerParameters,
    private val fetcher: BackgroundFetcher
) : Worker(context, workParams) {

    override fun doWork(): Result {
        val url = inputData.getString(WORK_URL)

        if (url != null) {
            fetcher.downloadMxTakaTakMedia(url)
                .execute()
                .body()?.let {
                    if (it.responseCode == "200") {
                        val data =
                            Data.Builder().putString(WORKER_KEY_DOWNLOAD_URL, it.data.mainVideo)
                                .build()
                        return Result.success(data)
                    }
                }
        }
        return Result.failure()
    }
}