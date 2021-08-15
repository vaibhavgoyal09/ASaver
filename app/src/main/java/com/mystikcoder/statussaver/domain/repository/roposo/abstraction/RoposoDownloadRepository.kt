package com.mystikcoder.statussaver.domain.repository.roposo.abstraction

import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse

interface RoposoDownloadRepository {

    suspend fun downloadRoposeFile(url: String): DownloadRequestResponse
}