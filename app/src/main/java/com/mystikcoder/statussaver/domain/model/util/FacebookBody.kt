package com.mystikcoder.statussaver.domain.model.util

import com.google.gson.annotations.SerializedName

data class FacebookBody(

    @SerializedName("fb_dtsg")
    val fbKey: String,

    @SerializedName("variables")
    val variables: String = "{\"bucketsCount\":200,\"initialBucketID\":null,\"pinnedIDs\":[\"\"],\"scale\":3}",

    @SerializedName("doc_id")
    val doc_id: String = "2893638314007950"
)
