package com.mystikcoder.statussaver.states.facebook

sealed class FacebookEvent {
    class Success(val fileName: String, val videoUrl: String) : FacebookEvent()
    class Failure(val errorText: String) : FacebookEvent()
    object Empty : FacebookEvent()
}