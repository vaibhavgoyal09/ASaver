package com.mystikcoder.statussaver.core.data.repository.sharechat.abstraction

import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse

interface ShareChatDownloadRepository {

    suspend fun downloadShareChatFile(url: String): DownloadRequestResponse
}