package com.mystikcoder.statussaver.domain.model.twitter

import com.google.gson.annotations.SerializedName

data class TwitterResponse(

    @SerializedName("videos")
    val videos: ArrayList<TwitterResponseModel>
)