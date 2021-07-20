package com.mystikcoder.statussaver.model.instagram

import com.google.gson.annotations.SerializedName

data class ItemModel(

    @SerializedName("taken_at")
    val takenAt: Long,

    @SerializedName("pk")
    val pk: Long,

    @SerializedName("id")
    val id: String,

    @SerializedName("device_timestamp")
    val timeStamp: Long,

    @SerializedName("media_type")
    val mediaType: Int,

    @SerializedName("code")
    val code: String,

    @SerializedName("client_cache_key")
    val clientCacheKey: String,

    @SerializedName("filter_type")
    val filterType: Int,

    @SerializedName("image_versions2")
    val imageVersions2: ImageVersionModel,

    @SerializedName("original_width")
    val originalWidth: Int,

    @SerializedName("original_height")
    val originalHeight: Int,

    @SerializedName("video_versions")
    val videoVersions: List<VideoVersionModel>,

    @SerializedName("has_audio")
    val hasVideo: Boolean,

    @SerializedName("video_duration")
    val videoDuration: Double,

    @SerializedName("caption_is_edited")
    val isCaptionEdited: Boolean,

    @SerializedName("caption_position")
    val captionPosition: Int,

    @SerializedName("is_reel_media")
    val isReelsMedia: Boolean,

    @SerializedName("photo_of_you")
    val photoOfYou: Boolean,

    @SerializedName("organic_tracking_token")
    val organicTrackingToken: String,

    @SerializedName("expiring_at")
    val expiringAt: Long,

    @SerializedName("can_reshare")
    val canReShare: Boolean,

    @SerializedName("can_reply")
    val canReply: Boolean
)