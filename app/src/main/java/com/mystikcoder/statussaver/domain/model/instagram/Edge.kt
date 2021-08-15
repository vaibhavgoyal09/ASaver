package com.mystikcoder.statussaver.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class Edge(

    @SerializedName("node")
    val node: Node
)