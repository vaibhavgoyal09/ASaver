package com.mystikcoder.statussaver.framework.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.mystikcoder.statussaver.core.data.repository.likee.abstraction.LikeeDownloadRepository
import com.mystikcoder.statussaver.framework.utils.WORKER_KEY_DOWNLOAD_URL
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LikeeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: LikeeDownloadRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val url = inputData.getString(WORKER_KEY_DOWNLOAD_URL)

        if (url != null) {

            val response = repository.downloadLikeeFile(url)

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