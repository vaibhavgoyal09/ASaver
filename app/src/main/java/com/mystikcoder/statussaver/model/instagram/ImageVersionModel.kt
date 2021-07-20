package com.mystikcoder.statussaver.model.instagram

import com.google.gson.annotations.SerializedName

data class ImageVersionModel(

    @SerializedName("candidates")
    val candidates: ArrayList<Candidates>
)