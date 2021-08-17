package com.mystikcoder.statussaver.data.repository.tiktok.abstraction

import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse

interface TiktokDownloadRepository {

    suspend fun downloadTiktokFile(url: String): DownloadRequestResponse
}