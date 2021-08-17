package com.mystikcoder.statussaver.data.repository.facebook.abstraction

import com.mystikcoder.statussaver.domain.model.facebook.FacebookNode
import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse
import com.mystikcoder.statussaver.presentation.utils.Resource

interface FacebookRepository {

    suspend fun downloadFacebookFile(url: String): DownloadRequestResponse

    suspend fun getUsers(cookies: String , fbKey: String): Resource<List<FacebookNode>>

    suspend fun getUserStories(cookies: String ,fbKey: String, userId: String): Resource<List<FacebookNode>>
}