package com.mystikcoder.statussaver.events

sealed class ChingariEvent {
    class Success(val fileName: String, val videoUrl: String) : ChingariEvent()
    class Failure(val errorText: String) : ChingariEvent()
    object Empty : ChingariEvent()
}