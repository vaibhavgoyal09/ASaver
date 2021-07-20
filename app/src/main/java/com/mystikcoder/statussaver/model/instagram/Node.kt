package com.mystikcoder.statussaver.model.instagram

import com.google.gson.annotations.SerializedName

data class Node(

    @SerializedName("display_url")
    val displayUrl: String,

    @SerializedName("display_resources")
    val displayResources: List<DisplayResource>,

    @SerializedName("is_video")
    val isVideo: Boolean,

    @SerializedName("video_url")
    val videoUrl: String
)