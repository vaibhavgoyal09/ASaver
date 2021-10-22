package com.mystikcoder.statussaver.framework.events.instagram

import com.mystikcoder.statussaver.core.domain.model.instagram.TrayModel

sealed class InstagramUserEvent {
    class Success(val data: List<TrayModel>?) : InstagramUserEvent()
    class Failure(val errorText: String) : InstagramUserEvent()
    object Loading : InstagramUserEvent()
    object Idle : InstagramUserEvent()
}