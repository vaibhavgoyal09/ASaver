package com.mystikcoder.statussaver.model.instagram

import com.google.gson.annotations.SerializedName

data class EdgeSidecarToChildren(

    @SerializedName("edges")
    val edges: List<Edge>
)