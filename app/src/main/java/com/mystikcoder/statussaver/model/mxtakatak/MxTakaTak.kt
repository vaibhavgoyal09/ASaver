package com.mystikcoder.statussaver.model.mxtakatak

import com.google.gson.annotations.SerializedName

data class MxTakaTak(

    @SerializedName("data")
    val data: MxTakaTakData,

    @SerializedName("description")
    val description: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("responsecode")
    val responseCode: String,

    @SerializedName("status")
    val status: String
)