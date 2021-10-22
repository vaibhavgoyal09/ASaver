package com.mystikcoder.statussaver.core.data.repository.twitter.abstraction

import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse

interface TwitterDownloadRepository {

    suspend fun downloadTwitterFile(
        url: String
    ): DownloadRequestResponse
}