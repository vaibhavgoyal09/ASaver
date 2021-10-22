package com.mystikcoder.statussaver.framework.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.mystikcoder.statussaver.core.data.repository.mxtakatak.abstraction.MxTakaTakDownloadRepository
import com.mystikcoder.statussaver.framework.utils.WORKER_KEY_DOWNLOAD_URL
import com.mystikcoder.statussaver.framework.utils.WORK_URL
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MxTakaTakWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workParams: WorkerParameters,
    private val repository: MxTakaTakDownloadRepository
) : CoroutineWorker(context, workParams) {

    override suspend fun doWork(): Result {

        val url = inputData.getString(WORK_URL)

        if (url != null) {
            val response = repository.downloadMxTakaTakFile(url)
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