package com.mystikcoder.statussaver.core.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class ResponseModel(

    @SerializedName("graphql")
    val graphQl: com.mystikcoder.statussaver.core.domain.model.instagram.GraphQl
)