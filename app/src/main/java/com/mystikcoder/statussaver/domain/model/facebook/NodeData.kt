package com.mystikcoder.statussaver.domain.model.facebook

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class NodeData(

    @SerializedName("attachments")
    val attachmentsList: ArrayList<Media>,

    @SerializedName("id")
    val id: String,

    @SerializedName("owner")
    val owner: JsonObject?,

    @SerializedName("story_bucket_owner")
    val storyBucketOwner: JsonObject
)