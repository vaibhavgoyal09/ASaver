package com.mystikcoder.statussaver.model.mxtakatak

import com.google.gson.annotations.SerializedName

data class MxTakaTakData(

    @SerializedName("mainvideo")
    val mainVideo: String,

    @SerializedName("thumbnail")
    val thumbnail: String,

    @SerializedName("userdetail")
    val userDetail: String,

    @SerializedName("videowithoutWaterMark")
    val videoWithoutWaterMark: String
)