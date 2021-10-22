package com.mystikcoder.statussaver.core.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class FullDetailModel(

    @SerializedName("user_detail")
    val userDetail: UserDetail,

    @SerializedName("reel_feed")
    val reelsFeed: ReelsFeed
)