package com.mystikcoder.statussaver.presentation.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.mystikcoder.statussaver.domain.repository.facebook.abstraction.FacebookRepository
import com.mystikcoder.statussaver.presentation.utils.WORKER_KEY_DOWNLOAD_URL
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FacebookWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: FacebookRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val url = inputData.getString(WORKER_KEY_DOWNLOAD_URL)

        if (url != null) {

            val response = repository.downloadFacebookFile(url)

            if (response.isSuccess) {
                val data =
                    Data.Builder().putString(WORKER_KEY_DOWNLOAD_URL, response.downloadLink!!)
                return Result.success(data.build())
            }
        }

        return Result.failure()
    }
}