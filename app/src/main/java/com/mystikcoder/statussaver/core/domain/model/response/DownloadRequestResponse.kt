package com.mystikcoder.statussaver.core.domain.model.response

data class DownloadRequestResponse(
    var isSuccess: Boolean = false,
    var errorMessage: String? = null,
    var downloadLink: String? = null
)
