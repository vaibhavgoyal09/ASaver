package com.mystikcoder.statussaver.core.data.repository.facebook.abstraction

import com.mystikcoder.statussaver.core.domain.model.facebook.FacebookNode
import com.mystikcoder.statussaver.core.domain.model.response.DownloadRequestResponse
import com.mystikcoder.statussaver.framework.utils.Resource

interface FacebookRepository {

    suspend fun downloadFacebookFile(url: String): DownloadRequestResponse

    suspend fun getUsers(cookies: String , fbKey: String): Resource<List<FacebookNode>>

    suspend fun getUserStories(cookies: String ,fbKey: String, userId: String): Resource<List<FacebookNode>>
}