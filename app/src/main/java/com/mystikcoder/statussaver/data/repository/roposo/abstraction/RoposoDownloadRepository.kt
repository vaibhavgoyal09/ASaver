package com.mystikcoder.statussaver.data.repository.roposo.abstraction

import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse

interface RoposoDownloadRepository {

    suspend fun downloadRoposeFile(url: String): DownloadRequestResponse
}