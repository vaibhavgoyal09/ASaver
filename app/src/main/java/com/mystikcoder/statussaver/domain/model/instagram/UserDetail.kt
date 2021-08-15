package com.mystikcoder.statussaver.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class UserDetail(

    @SerializedName("user")
    val user: User
)