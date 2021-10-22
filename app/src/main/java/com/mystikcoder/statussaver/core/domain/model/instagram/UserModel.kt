package com.mystikcoder.statussaver.core.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class UserModel(

    @SerializedName("pk")
    val pk: Long,

    @SerializedName("username")
    val userName: String,

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("is_private")
    val isPrivate: Boolean,

    @SerializedName("profile_pic_id")
    val profilePicId: String,

    @SerializedName("profile_pic_url")
    val profilePicUrl: String,

    @SerializedName("is_verified")
    val isVerified: Boolean
)