package com.mystikcoder.statussaver.states

sealed class ShareChatEvent {
    class Success(val fileName: String, val videoUrl: String) : ShareChatEvent()
    class Failure(val errorText: String) : ShareChatEvent()
    object Empty : ShareChatEvent()
}