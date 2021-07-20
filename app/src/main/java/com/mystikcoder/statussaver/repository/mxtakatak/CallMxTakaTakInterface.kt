package com.mystikcoder.statussaver.repository.mxtakatak

import com.mystikcoder.statussaver.model.mxtakatak.MxTakaTak
import com.mystikcoder.statussaver.utils.Resource

interface CallMxTakaTakInterface {

    suspend fun getTakaTakData(
        websiteUrl: String,
        mediaUrl: String
    ): Resource<MxTakaTak>
}