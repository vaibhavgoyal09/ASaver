package com.mystikcoder.statussaver.core.data.repository.mitron.abstraction

import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse

interface MitronDownloadRepository {

    suspend fun downloadMitronFile(url: String): DownloadRequestResponse
}