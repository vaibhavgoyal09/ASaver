package com.mystikcoder.statussaver.model.instagram

import com.google.gson.annotations.SerializedName

data class TrayModel(

    @SerializedName("id")
    val id: String,

    @SerializedName("user")
    val user: UserModel,

    @SerializedName("media_count")
    val mediaCount: Int,

    @SerializedName("items")
    val items: ArrayList<ItemModel>
)