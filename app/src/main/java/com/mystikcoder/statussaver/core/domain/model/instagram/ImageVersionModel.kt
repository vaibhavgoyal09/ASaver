package com.mystikcoder.statussaver.core.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class ImageVersionModel(

    @SerializedName("candidates")
    val candidates: ArrayList<Candidates>
)