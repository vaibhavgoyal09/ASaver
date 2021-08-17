package com.mystikcoder.statussaver.data.repository.josh.abstraction

import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse

interface JoshDownloadRepository {

    suspend fun downloadJoshFile(url: String): DownloadRequestResponse
}