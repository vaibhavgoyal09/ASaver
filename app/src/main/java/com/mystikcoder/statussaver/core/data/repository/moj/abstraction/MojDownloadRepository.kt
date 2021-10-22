package com.mystikcoder.statussaver.core.data.repository.moj.abstraction

import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse

interface MojDownloadRepository {

    suspend fun downloadMojFile(url: String): DownloadRequestResponse
}