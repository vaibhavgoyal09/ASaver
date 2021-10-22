package com.mystikcoder.statussaver.core.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class ShortcodeMedia(

    @SerializedName("display_url")
    val displayUrl: String,

    @SerializedName("display_resources")
    val displayResources: List<DisplayResource>,

    @SerializedName("is_video")
    val isVideo: Boolean,

    @SerializedName("edge_sidecar_to_children")
    val edgeSidecarToChildren: EdgeSidecarToChildren?,

    @SerializedName("video_url")
    val videoUrl: String,

    @SerializedName("accessibility_caption")
    val accessibilityCaption: String

)