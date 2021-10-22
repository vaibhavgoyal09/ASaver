package com.mystikcoder.statussaver.core.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class UserDetail(

    @SerializedName("user")
    val user: User
)