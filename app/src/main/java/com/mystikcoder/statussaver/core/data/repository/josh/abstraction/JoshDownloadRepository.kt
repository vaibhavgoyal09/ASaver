package com.mystikcoder.statussaver.core.data.repository.josh.abstraction

import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse

interface JoshDownloadRepository {

    suspend fun downloadJoshFile(url: String): DownloadRequestResponse
}