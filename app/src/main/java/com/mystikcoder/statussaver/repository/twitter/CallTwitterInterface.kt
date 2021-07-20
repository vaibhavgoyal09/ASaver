package com.mystikcoder.statussaver.repository.twitter

import com.mystikcoder.statussaver.model.twitter.TwitterResponse
import com.mystikcoder.statussaver.utils.Resource

interface CallTwitterInterface {

    suspend fun callTwitter(
        url: String,
        id: String
    ): Resource<TwitterResponse>
}