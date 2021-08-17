package com.mystikcoder.statussaver.data.repository.mxtakatak.abstraction

import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse

interface MxTakaTakDownloadRepository {

    suspend fun downloadMxTakaTakFile(
        mediaUrl: String
    ): DownloadRequestResponse
}