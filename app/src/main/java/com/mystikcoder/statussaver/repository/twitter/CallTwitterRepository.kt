package com.mystikcoder.statussaver.repository.twitter

import com.mystikcoder.statussaver.model.twitter.TwitterResponse
import com.mystikcoder.statussaver.networking.ApiService
import com.mystikcoder.statussaver.utils.Resource
import retrofit2.Response
import javax.inject.Inject

class CallTwitterRepository @Inject constructor(
    private val apiService: ApiService
) : CallTwitterInterface {
    override suspend fun callTwitter(
        url: String,
        id: String
    ): Resource<TwitterResponse> {

        val response: Response<TwitterResponse> =  apiService.callTwitter(url, id)
        val result = response.body()

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