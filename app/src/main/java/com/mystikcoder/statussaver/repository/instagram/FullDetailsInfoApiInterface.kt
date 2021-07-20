package com.mystikcoder.statussaver.repository.instagram

import com.mystikcoder.statussaver.model.instagram.FullDetailModel
import com.mystikcoder.statussaver.utils.Resource

interface FullDetailsInfoApiInterface {

    suspend fun getFullDetailInfoApi(
        value: String,
        cookie: String,
        userAgent: String
    ) :Resource<FullDetailModel>
}