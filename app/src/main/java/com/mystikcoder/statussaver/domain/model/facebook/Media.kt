package com.mystikcoder.statussaver.domain.model.facebook

import com.google.gson.annotations.SerializedName

data class Media(

    @SerializedName("media")
    val mediaData: MediaData
)