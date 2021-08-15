package com.mystikcoder.statussaver.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class GraphQl(
    @SerializedName("shortcode_media")
    val shortcodeMedia: ShortcodeMedia
)