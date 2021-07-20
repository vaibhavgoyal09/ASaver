package com.mystikcoder.statussaver.repository.instagram

import com.mystikcoder.statussaver.model.instagram.StoryModel
import com.mystikcoder.statussaver.networking.ApiService
import com.mystikcoder.statussaver.utils.Resource
import retrofit2.Response
import javax.inject.Inject

class StoriesApiRepository @Inject constructor(
    private val apiService: ApiService
) : StoriesApiInterface {
    override suspend fun getStoriesApi(
        value: String,
        cookie: String,
        userAgent: String
    ): Resource<StoryModel> {

        val response : Response<StoryModel> = apiService.getStoriesApi(value, cookie, userAgent)
        val result =response.body()

        return try {
            if (response.isSuccessful && result != null){
                Resource.Success(result)
            }else{
                Resource.Error(response.message())
            }
        }catch (e: Exception){
            return Resource.Error(e.message ?: "An error occurred")
        }
    }
}
