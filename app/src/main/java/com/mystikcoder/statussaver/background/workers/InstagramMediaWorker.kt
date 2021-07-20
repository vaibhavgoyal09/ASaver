package com.mystikcoder.statussaver.background.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mystikcoder.statussaver.background.repository.BackgroundFetcher
import com.mystikcoder.statussaver.model.instagram.Edge
import com.mystikcoder.statussaver.model.instagram.EdgeSidecarToChildren
import com.mystikcoder.statussaver.model.instagram.ResponseModel
import com.mystikcoder.statussaver.utils.WORKER_KEY_DOWNLOAD_URL
import com.mystikcoder.statussaver.utils.WORK_COOKIES
import com.mystikcoder.statussaver.utils.WORK_URL
import com.mystikcoder.statussaver.utils.WORK_USER_AGENT
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class InstagramMediaWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fetcher: BackgroundFetcher
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.e("Workers", "Insta worker called")

        val url = inputData.getString(WORK_URL)
        val cookies = inputData.getString(WORK_COOKIES)
        val userAgent = inputData.getString(WORK_USER_AGENT)
        val downloadUrls: ArrayList<String> = ArrayList()

        if (url != null && cookies != null && userAgent != null) {
            try {

                val response =
                    fetcher.downloadInstagramMedia(url, cookies, userAgent).execute().body()

                val responseModel: ResponseModel =
                    Gson().fromJson(
                        response.toString(),
                        object : TypeToken<ResponseModel>() {}.type
                    ) as ResponseModel

                val edgeSidecarToChildren: EdgeSidecarToChildren? =
                    responseModel.graphQl.shortcodeMedia.edgeSidecarToChildren

                var mediaUrl: String

                if (edgeSidecarToChildren != null) {

                    val edgeArrayList: List<Edge> = edgeSidecarToChildren.edges

                    for (edge in edgeArrayList) {
                        if (edge.node.isVideo) {
                            mediaUrl = edge.node.videoUrl
                            downloadUrls.add(mediaUrl)
                        } else {
                            mediaUrl =
                                edge.node.displayResources[2].src
                            downloadUrls.add(mediaUrl)
                        }
                    }
                } else {
                    if (responseModel.graphQl.shortcodeMedia.isVideo) {
                        mediaUrl = responseModel.graphQl.shortcodeMedia.videoUrl

                        downloadUrls.add(mediaUrl)

                    } else {
                        mediaUrl =
                            responseModel.graphQl.shortcodeMedia.displayResources[2].src

                        downloadUrls.add(mediaUrl)
                    }
                }
                val data = Data.Builder().putStringArray(WORKER_KEY_DOWNLOAD_URL, downloadUrls.toTypedArray())
                return Result.success(data.build())

            } catch (e: Exception) {
                return Result.failure()
            }
        } else {
            return Result.failure()
        }
    }
}
