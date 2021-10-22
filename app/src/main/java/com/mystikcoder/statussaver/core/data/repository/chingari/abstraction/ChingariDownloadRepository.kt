package com.mystikcoder.statussaver.core.data.repository.chingari.abstraction

import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse

interface ChingariDownloadRepository {

    suspend fun downloadChingariFile(url: String): DownloadRequestResponse
}