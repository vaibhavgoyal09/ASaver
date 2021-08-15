package com.mystikcoder.statussaver.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class DisplayResource(

    @SerializedName("src")
    val src: String,

    @SerializedName("config_width")
    val configWidth: Int,

    @SerializedName("config_height")
    val configHeight: Int
)