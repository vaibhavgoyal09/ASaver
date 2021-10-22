package com.mystikcoder.statussaver.framework.events.facebook

import com.mystikcoder.statussaver.core.domain.model.facebook.FacebookNode

sealed class FacebookUserStoriesEvent {
    class Success(val list: List<FacebookNode>?) : FacebookUserStoriesEvent()
    class Failure(val errorText: String) : FacebookUserStoriesEvent()
    object Idle : FacebookUserStoriesEvent()
    object Loading : FacebookUserStoriesEvent()
}