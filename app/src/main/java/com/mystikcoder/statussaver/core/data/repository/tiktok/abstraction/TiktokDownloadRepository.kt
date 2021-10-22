package com.mystikcoder.statussaver.core.data.repository.tiktok.abstraction

import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse

interface TiktokDownloadRepository {

    suspend fun downloadTiktokFile(url: String): DownloadRequestResponse
}