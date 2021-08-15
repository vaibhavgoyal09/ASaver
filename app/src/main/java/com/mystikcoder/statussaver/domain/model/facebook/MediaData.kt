package com.mystikcoder.statussaver.domain.model.facebook

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class MediaData(

    @SerializedName("__typename")
    val typeName: String,

    @SerializedName("id")
    val id: String,

    @SerializedName("playable_url_quality_hd")
    val playableUrlQualityHd: String,

    @SerializedName("previewImage")
    val previewImage: JsonObject
)