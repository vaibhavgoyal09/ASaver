package com.mystikcoder.statussaver.core.data.repository.likee.abstraction

import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse

interface LikeeDownloadRepository {

    suspend fun downloadLikeeFile(url: String): DownloadRequestResponse
}