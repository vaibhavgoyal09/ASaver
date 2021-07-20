package com.mystikcoder.statussaver.repository.instagram

import com.google.gson.JsonObject
import com.mystikcoder.statussaver.utils.Resource

interface CallResultInterface {

    suspend fun callResult(
        value: String,
        cookie: String,
        userAgent: String
    ): Resource<JsonObject>
}