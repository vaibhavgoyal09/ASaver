package com.mystikcoder.statussaver.states.instagram

import com.mystikcoder.statussaver.model.instagram.ItemModel

sealed class InstagramStoryDetailEvent {
    class Success(val data: ArrayList<ItemModel>?) : InstagramStoryDetailEvent()
    class Failure(val errorText: String) : InstagramStoryDetailEvent()
    object Loading : InstagramStoryDetailEvent()
    object Empty : InstagramStoryDetailEvent()
}