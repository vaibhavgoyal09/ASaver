package com.mystikcoder.statussaver.states

sealed class TikTokEvent {
    class Success(val fileName: String, val mediaUrl: String) : TikTokEvent()
    class Failure(val errorText: String) : TikTokEvent()
    object Loading : TikTokEvent()
    object Empty : TikTokEvent()
}
