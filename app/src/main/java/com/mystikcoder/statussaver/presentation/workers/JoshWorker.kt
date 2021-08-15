package com.mystikcoder.statussaver.presentation.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.mystikcoder.statussaver.domain.repository.josh.abstraction.JoshDownloadRepository
import com.mystikcoder.statussaver.presentation.utils.WORKER_KEY_DOWNLOAD_URL
import com.mystikcoder.statussaver.presentation.utils.WORK_URL
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class JoshWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: JoshDownloadRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        val link = inputData.getString(WORK_URL)

        if (link != null) {

            val response = repository.downloadJoshFile(link)

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