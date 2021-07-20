package com.mystikcoder.statussaver.repository.instagram

import com.mystikcoder.statussaver.model.instagram.StoryModel
import com.mystikcoder.statussaver.utils.Resource

interface StoriesApiInterface {

    suspend fun getStoriesApi(
        value: String,
        cookie: String,
        userAgent: String
    ): Resource<StoryModel>

}