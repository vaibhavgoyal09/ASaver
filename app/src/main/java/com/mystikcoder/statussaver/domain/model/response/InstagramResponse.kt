package com.mystikcoder.statussaver.domain.model.response

data class InstagramResponse(
    var isSuccess: Boolean = false,
    var errorMessage: String? = null,
    var downloadUrls: List<String>? = null
)
