package com.mystikcoder.statussaver.states.instagram

import com.mystikcoder.statussaver.model.instagram.TrayModel

sealed class InstagramStoryEvent {
    class Success(val data: ArrayList<TrayModel>?) : InstagramStoryEvent()
    class Failure(val errorText: String) : InstagramStoryEvent()
    object Loading : InstagramStoryEvent()
    object Empty : InstagramStoryEvent()
}