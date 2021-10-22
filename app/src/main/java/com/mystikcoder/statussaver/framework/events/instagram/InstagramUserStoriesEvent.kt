package com.mystikcoder.statussaver.framework.events.instagram

import com.mystikcoder.statussaver.core.domain.model.instagram.ItemModel

sealed class InstagramUserStoriesEvent {
    class Success(val data: List<ItemModel>?) : InstagramUserStoriesEvent()
    class Failure(val errorText: String) : InstagramUserStoriesEvent()
    object Loading : InstagramUserStoriesEvent()
    object Idle : InstagramUserStoriesEvent()
}