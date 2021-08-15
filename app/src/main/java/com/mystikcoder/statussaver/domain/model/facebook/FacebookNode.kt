package com.mystikcoder.statussaver.domain.model.facebook

import com.google.gson.annotations.SerializedName

data class FacebookNode(

    @SerializedName("node")
    val nodeData: NodeData
)