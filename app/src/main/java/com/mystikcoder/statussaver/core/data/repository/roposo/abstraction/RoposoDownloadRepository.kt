package com.mystikcoder.statussaver.core.data.repository.roposo.abstraction

import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse

interface RoposoDownloadRepository {

    suspend fun downloadRoposeFile(url: String): DownloadRequestResponse
}