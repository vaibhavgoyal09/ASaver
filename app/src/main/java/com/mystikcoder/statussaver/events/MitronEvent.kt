package com.mystikcoder.statussaver.events

sealed class MitronEvent {
    class Success(val fileName: String, val videoUrl: String) : MitronEvent()
    class Failure(val errorText: String) : MitronEvent()
    object Empty : MitronEvent()
}