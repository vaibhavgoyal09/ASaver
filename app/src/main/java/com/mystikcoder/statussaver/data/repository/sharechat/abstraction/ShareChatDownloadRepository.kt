package com.mystikcoder.statussaver.data.repository.sharechat.abstraction

import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse

interface ShareChatDownloadRepository {

    suspend fun downloadShareChatFile(url: String): DownloadRequestResponse
}