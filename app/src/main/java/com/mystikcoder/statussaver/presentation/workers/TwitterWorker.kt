package com.mystikcoder.statussaver.presentation.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.mystikcoder.statussaver.domain.repository.twitter.abstraction.TwitterDownloadRepository
import com.mystikcoder.statussaver.presentation.utils.WORKER_KEY_DOWNLOAD_URL
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TwitterWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workParams: WorkerParameters,
    private val repository: TwitterDownloadRepository
) : CoroutineWorker(context, workParams) {

    override suspend fun doWork(): Result {

        val url = inputData.getString(WORKER_KEY_DOWNLOAD_URL)

        if (url != null) {

            val response = repository.downloadTwitterFile(url)

            if (response.isSuccess) {
                val data =
                    Data.Builder().putString(WORKER_KEY_DOWNLOAD_URL, response.downloadLink!!)
                        .build()
                return Result.success(data)
            }
        }
        return Result.failure()
    }
}
