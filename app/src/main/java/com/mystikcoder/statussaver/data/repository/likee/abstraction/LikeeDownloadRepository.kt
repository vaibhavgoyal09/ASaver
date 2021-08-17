package com.mystikcoder.statussaver.data.repository.likee.abstraction

import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse

interface LikeeDownloadRepository {

    suspend fun downloadLikeeFile(url: String): DownloadRequestResponse
}