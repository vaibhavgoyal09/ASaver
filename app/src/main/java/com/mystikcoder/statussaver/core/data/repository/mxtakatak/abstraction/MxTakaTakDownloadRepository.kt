package com.mystikcoder.statussaver.core.data.repository.mxtakatak.abstraction

import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse

interface MxTakaTakDownloadRepository {

    suspend fun downloadMxTakaTakFile(
        mediaUrl: String
    ): DownloadRequestResponse
}