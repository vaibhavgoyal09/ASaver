package com.mystikcoder.statussaver.domain.repository.twitter.abstraction

import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse

interface TwitterDownloadRepository {

    suspend fun downloadTwitterFile(
        url: String
    ): DownloadRequestResponse
}