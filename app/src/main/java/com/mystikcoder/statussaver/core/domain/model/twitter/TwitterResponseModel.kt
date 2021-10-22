package com.mystikcoder.statussaver.core.domain.model.twitter

import com.google.gson.annotations.SerializedName

data class TwitterResponseModel(

    @SerializedName("source")
    val source: String,

    @SerializedName("text")
    val text: String,

    @SerializedName("thumb")
    val thumb: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("duration")
    val duration: Int,

    @SerializedName("bitrate")
    val bitrate: Int,

    @SerializedName("url")
    val url: String,

    @SerializedName("size")
    val size: Int

)