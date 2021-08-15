package com.mystikcoder.statussaver.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class ReelsFeed(

    @SerializedName("id")
    val id: Long,

    @SerializedName("latest_reel_media")
    val latestReelsMedia: Long,

    @SerializedName("expiring_atexpiring_at")
    val expiringAt: Long,

    @SerializedName("seen")
    val seen: Long,

    @SerializedName("reel_type")
    val reelsType: String,

    @SerializedName("items")
    val items: ArrayList<ItemModel>,

    @SerializedName("media_count")
    val mediaCount: Int
)