package com.mystikcoder.statussaver.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class ResponseModel(

    @SerializedName("graphql")
    val graphQl: GraphQl
)