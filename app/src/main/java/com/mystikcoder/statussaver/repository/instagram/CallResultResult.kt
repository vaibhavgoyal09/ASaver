package com.mystikcoder.statussaver.repository.instagram

import com.google.gson.JsonObject
import com.mystikcoder.statussaver.networking.ApiService
import com.mystikcoder.statussaver.utils.Resource
import retrofit2.Response
import javax.inject.Inject

class CallResultResult @Inject constructor(
    private val apiService: ApiService
) : CallResultInterface {
    override suspend fun callResult(
        value: String,
        cookie: String,
        userAgent: String
    ): Resource<JsonObject> {

        val response: Response<JsonObject> = apiService.callResult(value, cookie, userAgent)
        val result = response.body()

        return try {

            if (response.isSuccessful && result != null) {
                Resource.Success(result)
            } else {
                Resource.Error(response.message())
            }
        }catch (e: Exception){
            return Resource.Error(e.message ?: "An error occurred")
        }
    }
}