package com.mystikcoder.statussaver.core.domain.model.facebook

import com.google.gson.annotations.SerializedName

data class FacebookNode(

    @SerializedName("node")
    val nodeData: NodeData
)