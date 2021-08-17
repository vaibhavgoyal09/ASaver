package com.mystikcoder.statussaver.presentation.framework.events.facebook

import com.mystikcoder.statussaver.domain.model.facebook.FacebookNode

sealed class FacebookUserEvent {
    class Success(val list: List<FacebookNode>?) : FacebookUserEvent()
    class Failure(val errorText: String) : FacebookUserEvent()
    object Idle : FacebookUserEvent()
    object Loading : FacebookUserEvent()
}