package com.mystikcoder.statussaver.events.instagram

import com.mystikcoder.statussaver.model.instagram.TrayModel

sealed class InstagramStoryEvent {
    class Success(val data: ArrayList<TrayModel>?) : InstagramStoryEvent()
    class Failure(val errorText: String) : InstagramStoryEvent()
    object Loading : InstagramStoryEvent()
    object Empty : InstagramStoryEvent()
}