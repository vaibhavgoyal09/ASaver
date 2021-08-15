package com.mystikcoder.statussaver.presentation.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.mystikcoder.statussaver.domain.repository.sharechat.abstraction.ShareChatDownloadRepository
import com.mystikcoder.statussaver.presentation.utils.WORKER_KEY_DOWNLOAD_URL
import com.mystikcoder.statussaver.presentation.utils.WORK_URL
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ShareChatMediaWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: ShareChatDownloadRepository
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {

        val url = inputData.getString(WORK_URL)

        if (url != null) {

            val response = repository.downloadShareChatFile(url)

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
