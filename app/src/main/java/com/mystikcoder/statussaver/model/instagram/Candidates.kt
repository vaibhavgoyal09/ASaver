package com.mystikcoder.statussaver.model.instagram

import com.google.gson.annotations.SerializedName

data class Candidates(

    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int,

    @SerializedName("url")
    val url: String,

    @SerializedName("scans_profile")
    val scansProfile: String
)