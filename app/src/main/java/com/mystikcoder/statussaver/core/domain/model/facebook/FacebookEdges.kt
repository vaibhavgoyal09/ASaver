package com.mystikcoder.statussaver.core.domain.model.facebook

import com.google.gson.annotations.SerializedName

data class FacebookEdges(

    @SerializedName("edges")
    val edgesModel: ArrayList<FacebookNode>
)