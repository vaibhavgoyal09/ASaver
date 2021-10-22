package com.mystikcoder.statussaver.core.domain.model.facebook

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class NodeData(

    @SerializedName("attachments")
    val attachmentsList: ArrayList<com.mystikcoder.statussaver.core.domain.model.facebook.Media>,

    @SerializedName("id")
    val id: String,

    @SerializedName("owner")
    val owner: JsonObject?,

    @SerializedName("story_bucket_owner")
    val storyBucketOwner: JsonObject
)