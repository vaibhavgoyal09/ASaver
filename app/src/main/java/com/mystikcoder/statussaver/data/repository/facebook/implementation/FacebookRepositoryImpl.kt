package com.mystikcoder.statussaver.data.repository.facebook.implementation

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mystikcoder.statussaver.data.networking.FacebookApiService
import com.mystikcoder.statussaver.data.repository.facebook.abstraction.FacebookRepository
import com.mystikcoder.statussaver.domain.model.facebook.FacebookEdges
import com.mystikcoder.statussaver.domain.model.facebook.FacebookNode
import com.mystikcoder.statussaver.domain.model.response.DownloadRequestResponse
import com.mystikcoder.statussaver.domain.model.util.FacebookBody
import com.mystikcoder.statussaver.presentation.utils.FACEBOOK_USER_AGENT
import com.mystikcoder.statussaver.presentation.utils.Resource
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

class FacebookRepositoryImpl @Inject constructor(
    private val api: FacebookApiService
) : FacebookRepository {

    override suspend fun downloadFacebookFile(url: String): DownloadRequestResponse {
        kotlin.runCatching {

            val document: Document = Jsoup.connect(url).get()

            val videoUrl =
                document.select("meta[property=\"og:video:url\"]")
                    ?.last()
                    ?.attr("content")

            return if (!videoUrl.isNullOrEmpty()) {
                DownloadRequestResponse(isSuccess = true, downloadLink = videoUrl)
            } else {
                DownloadRequestResponse(errorMessage = "No data found")
            }

        }.getOrElse {
            return DownloadRequestResponse(errorMessage = it.message ?: "Something Went Wrong")
        }
    }

    override suspend fun getUsers(cookies: String, fbKey: String): Resource<List<FacebookNode>> {
        val response = api.getUsers(cookies, FACEBOOK_USER_AGENT, FacebookBody(fbKey))
        val result = response.body()

        val edgeModelList: MutableList<FacebookNode> = ArrayList()

        if (response.isSuccessful && result != null) {

            try {
                val tempResponse = result
                    .getJSONObject("data")
                    .getJSONObject("me")
                    .getJSONObject("unified_stories_buckets")

                val edges: FacebookEdges = Gson()
                    .fromJson(
                        tempResponse.toString(),
                        object : TypeToken<FacebookEdges>() {}.type
                    ) as FacebookEdges

                return if (edges.edgesModel.size > 0) {
                    edgeModelList.clear()
                    edgeModelList.addAll(edges.edgesModel)
                    Resource.Success(edgeModelList)
                } else {
                    Resource.Error("No data found")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return Resource.Error(e.message ?: "Something Went Wrong")
            }

        } else {
            return Resource.Error(response.message())
        }
    }

    override suspend fun getUserStories(
        cookies: String,
        fbKey: String,
        userId: String
    ): Resource<List<FacebookNode>> {
        val response = api.getUserStories(
            cookies, FACEBOOK_USER_AGENT, FacebookBody(
                fbKey,
                "{\"bucketID\":\"$userId\",\"initialBucketID\":\"$userId\",\"initialLoad\":false,\"scale\":5}"
            )
        )
        val result = response.body()

        if (response.isSuccessful && result != null) {

            try {
                val tempJson = result
                    .getJSONObject("data")
                    .getJSONObject("bucket")
                    .getJSONObject("unified_stories")

                val edgeModel = Gson().fromJson(
                    tempJson.toString(),
                    object : TypeToken<FacebookEdges>() {}.type
                ) as FacebookEdges

                edgeModel.edgesModel[0].nodeData.attachmentsList
                return Resource.Success(edgeModel.edgesModel)

            } catch (e: Exception) {
                e.printStackTrace()
                return Resource.Error(e.message ?: "Something Went Wrong")
            }

        } else {
            return Resource.Error(response.message())
        }
    }
}