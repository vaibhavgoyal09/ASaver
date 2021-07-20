package com.mystikcoder.statussaver.model.instagram

import com.google.gson.annotations.SerializedName

data class UserDetail(

    @SerializedName("user")
    val user: User
)