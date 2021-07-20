package com.mystikcoder.statussaver.model.instagram

import com.google.gson.annotations.SerializedName

data class HdProfile(

    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int,

    @SerializedName("url")
    val url: String
)