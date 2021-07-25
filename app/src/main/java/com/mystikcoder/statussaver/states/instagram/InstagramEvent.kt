package com.mystikcoder.statussaver.states.instagram

sealed class InstagramEvent {
    class Success(val mediaUrl: ArrayList<String>?) : InstagramEvent()
    class Failure(val errorText: String) : InstagramEvent()
    object Loading : InstagramEvent()
    object Empty : InstagramEvent()
}