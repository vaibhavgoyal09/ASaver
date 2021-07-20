package com.mystikcoder.statussaver.repository.mxtakatak

import com.mystikcoder.statussaver.model.mxtakatak.MxTakaTak
import com.mystikcoder.statussaver.networking.ApiService
import com.mystikcoder.statussaver.utils.Resource
import javax.inject.Inject

class CallMxTakaTakRepository @Inject constructor(
    private val apiService: ApiService
) : CallMxTakaTakInterface{
    override suspend fun getTakaTakData(websiteUrl: String, mediaUrl: String): Resource<MxTakaTak> {
        val response = apiService.getMxTakaTak(websiteUrl, mediaUrl)
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