package com.mystikcoder.statussaver.model.instagram

import com.google.gson.annotations.SerializedName

data class VideoVersionModel(

    @SerializedName("type")
    val type: Int,

    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int,

    @SerializedName("url")
    val url: String,

    @SerializedName("id")
    val id: String
)