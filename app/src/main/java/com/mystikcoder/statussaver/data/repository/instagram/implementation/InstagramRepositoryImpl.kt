package com.mystikcoder.statussaver.data.repository.instagram.implementation

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.mystikcoder.statussaver.data.networking.ApiService
import com.mystikcoder.statussaver.data.repository.instagram.abstraction.InstagramRepository
import com.mystikcoder.statussaver.domain.model.instagram.*
import com.mystikcoder.statussaver.domain.model.response.InstagramResponse
import com.mystikcoder.statussaver.presentation.utils.INSTAGRAM_USER_AGENT
import com.mystikcoder.statussaver.presentation.utils.Resource
import com.mystikcoder.statussaver.presentation.utils.Utils
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class InstagramRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : InstagramRepository {

    override suspend fun downloadInstagramFile(url: String, cookie: String): InstagramResponse {

        val urls: MutableList<String> = ArrayList()

        val response: Response<JsonObject> = apiService.callResult(
            url,
            if (Utils.isNullOrEmpty(cookie)) "" else cookie,
            INSTAGRAM_USER_AGENT
        )

        val result = response.body()

        try {

            if (response.isSuccessful && result != null) {

                Timber.e(result.toString())

                val responseModel: ResponseModel =
                    Gson().fromJson(result,object : TypeToken<ResponseModel>() {}.type) as ResponseModel

                val edgeSidecarToChildren: EdgeSidecarToChildren? =
                    responseModel.graphQl.shortcodeMedia.edgeSidecarToChildren

                var mediaUrl: String

                if (edgeSidecarToChildren != null) {

                    val edgeArrayList: List<Edge> = edgeSidecarToChildren.edges

                    for (edge in edgeArrayList) {
                        if (edge.node.isVideo) {
                            mediaUrl = edge.node.videoUrl
                            urls.add(mediaUrl)
                        } else {
                            mediaUrl =
                                edge.node.displayResources[2].src
                            urls.add(mediaUrl)
                        }
                    }
                } else {
                    if (responseModel.graphQl.shortcodeMedia.isVideo) {
                        mediaUrl = responseModel.graphQl.shortcodeMedia.videoUrl

                        urls.add(mediaUrl)

                    } else {
                        mediaUrl =
                            responseModel.graphQl.shortcodeMedia.displayResources[2].src

                        urls.add(mediaUrl)
                    }
                }

                return InstagramResponse(isSuccess = true, downloadUrls = urls)

            } else {
                return InstagramResponse(isSuccess = false, errorMessage = "No data found")
            }

        } catch (e: Exception) {
            Timber.e(e.message)
            return InstagramResponse(
                errorMessage = e.message ?: "Something Went Wrong"
            )
        }
    }

    override suspend fun getUserStories(
        cookie: String,
        userId: String
    ): Resource<List<ItemModel>?> {

        val response: Response<FullDetailModel> =
            apiService.getFullDetailInfoApi(
                "https://i.instagram.com/api/v1/users/$userId/full_detail_info?max_id=",
                cookie,
                "\"$INSTAGRAM_USER_AGENT\""
            )
        val result = response.body()

        return try {
            if (response.isSuccessful && result != null) {
                Resource.Success(result.reelsFeed.items)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            return Resource.Error(e.message ?: "An error occurred")
        }
    }

    override suspend fun getUsers(cookie: String): Resource<List<TrayModel>> {

        val response: Response<StoryModel> = apiService.getStoriesApi(
            "https://i.instagram.com/api/v1/feed/reels_tray/",
            if (Utils.isNullOrEmpty(cookie)) "" else cookie,
            "\"$INSTAGRAM_USER_AGENT\""
        )
        val result = response.body()

        return try {
            if (response.isSuccessful && result != null) {
                Resource.Success(result.tray)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            return Resource.Error(e.message ?: "An error occurred")
        }
    }
}