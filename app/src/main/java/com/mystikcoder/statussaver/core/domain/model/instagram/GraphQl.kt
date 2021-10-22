package com.mystikcoder.statussaver.core.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class GraphQl(
    @SerializedName("shortcode_media")
    val shortcodeMedia: ShortcodeMedia
)