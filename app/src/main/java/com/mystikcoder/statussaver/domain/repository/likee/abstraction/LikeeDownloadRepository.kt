package com.mystikcoder.statussaver.domain.repository.likee.abstraction

import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse

interface LikeeDownloadRepository {

    suspend fun downloadLikeeFile(url: String): DownloadRequestResponse
}