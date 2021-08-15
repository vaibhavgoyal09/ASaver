package com.mystikcoder.statussaver.domain.events.instagram

import com.mystikcoder.statussaver.domain.model.instagram.ItemModel

sealed class InstagramUserStoriesEvent {
    class Success(val data: List<ItemModel>?) : InstagramUserStoriesEvent()
    class Failure(val errorText: String) : InstagramUserStoriesEvent()
    object Loading : InstagramUserStoriesEvent()
    object Idle : InstagramUserStoriesEvent()
}