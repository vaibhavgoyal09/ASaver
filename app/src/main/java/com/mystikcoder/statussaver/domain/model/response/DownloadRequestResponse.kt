package com.mystikcoder.statussaver.domain.model.response

data class DownloadRequestResponse(
    var isSuccess: Boolean = false,
    var errorMessage: String? = null,
    var downloadLink: String? = null
)
