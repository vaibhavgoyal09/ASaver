package com.mystikcoder.statussaver.model.instagram

import com.google.gson.annotations.SerializedName

data class GraphQl(
    @SerializedName("shortcode_media")
    val shortcodeMedia: ShortcodeMedia
)