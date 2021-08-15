package com.mystikcoder.statussaver.domain.repository.moj.abstraction

import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse

interface MojDownloadRepository {

    suspend fun downloadMojFile(url: String): DownloadRequestResponse
}