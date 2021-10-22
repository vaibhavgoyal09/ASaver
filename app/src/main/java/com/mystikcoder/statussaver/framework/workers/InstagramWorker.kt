package com.mystikcoder.statussaver.framework.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.mystikcoder.statussaver.core.data.repository.instagram.abstraction.InstagramRepository
import com.mystikcoder.statussaver.framework.utils.WORKER_KEY_DOWNLOAD_URL
import com.mystikcoder.statussaver.framework.utils.WORK_COOKIES
import com.mystikcoder.statussaver.framework.utils.WORK_URL
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class InstagramWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: InstagramRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        val url = inputData.getString(WORK_URL)
        val cookies = inputData.getString(WORK_COOKIES)
        val downloadUrls: MutableList<String> = ArrayList()

        if (url != null && cookies != null) {

            val response = repository.downloadInstagramFile(url, cookies)

            if (response.isSuccess) {
                downloadUrls.addAll(response.downloadUrls!!)

                val data =
                    Data.Builder().putStringArray(WORKER_KEY_DOWNLOAD_URL, downloadUrls.toTypedArray())

                return Result.success(data.build())
            }

        }
        return Result.failure()
    }
}
