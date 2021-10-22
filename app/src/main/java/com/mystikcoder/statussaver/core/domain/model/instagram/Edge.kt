package com.mystikcoder.statussaver.core.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class Edge(

    @SerializedName("node")
    val node: Node
)