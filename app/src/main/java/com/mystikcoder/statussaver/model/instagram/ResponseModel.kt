package com.mystikcoder.statussaver.model.instagram

import com.google.gson.annotations.SerializedName

data class ResponseModel(

    @SerializedName("graphql")
    val graphQl: GraphQl
)