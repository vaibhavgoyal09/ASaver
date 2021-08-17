package com.mystikcoder.statussaver.data.repository.instagram.abstraction

import com.mystikcoder.statussaver.domain.model.instagram.ItemModel
import com.mystikcoder.statussaver.domain.model.instagram.TrayModel
import com.mystikcoder.statussaver.domain.model.response.InstagramResponse
import com.mystikcoder.statussaver.presentation.utils.Resource

interface InstagramRepository {

    suspend fun downloadInstagramFile(
        url: String,
        cookie: String
    ): InstagramResponse

    suspend fun getUserStories(
        cookie: String,
        userId: String
    ) :Resource<List<ItemModel>?>

    suspend fun getUsers(
        cookie: String
    ): Resource<List<TrayModel>>

}