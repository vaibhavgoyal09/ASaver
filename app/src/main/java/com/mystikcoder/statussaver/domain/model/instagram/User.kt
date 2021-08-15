package com.mystikcoder.statussaver.domain.model.instagram

import com.google.gson.annotations.SerializedName

data class User(

    @SerializedName("pk")
    val pk: Long,

    @SerializedName("username")
    val userName:String,

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("is_private")
    val isPrivate: Boolean,

    @SerializedName("profile_pic_url")
    val profilePicUrl: String,

    @SerializedName("profile_pic_id")
    val profilePicId: String,

    @SerializedName("is_verified")
    val isVerified: Boolean,

    @SerializedName("media_count")
    val mediaCount: Int,

    @SerializedName("follower_count")
    val followerCount: Int,

    @SerializedName("following_count")
    val followingCount: Int,

    @SerializedName("biography")
    val biography: String,

    @SerializedName("total_igtv_videos")
    val totalIGTVVideos: String,

    @SerializedName("hd_profile_pic_url_info")
    val hdProfilePicUrlInfo: HdProfile,

    @SerializedName("mutual_followers_count")
    val mutualFollowersCount: Int,

    @SerializedName("profile_context")
    val profileContext: String
)