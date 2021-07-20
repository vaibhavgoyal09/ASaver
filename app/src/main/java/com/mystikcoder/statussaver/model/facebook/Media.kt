package com.mystikcoder.statussaver.model.facebook

import com.google.gson.annotations.SerializedName

data class Media(

    @SerializedName("media")
    val mediaData: MediaData
)