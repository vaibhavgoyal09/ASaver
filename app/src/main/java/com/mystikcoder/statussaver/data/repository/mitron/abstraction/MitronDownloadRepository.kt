package com.mystikcoder.statussaver.data.repository.mitron.abstraction

import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse

interface MitronDownloadRepository {

    suspend fun downloadMitronFile(url: String): DownloadRequestResponse
}