package com.mystikcoder.statussaver.events

sealed class LikeeEvent {
    class Success(val videoUrl: String, val fileName: String) : LikeeEvent()
    class Failure(val errorText: String) : LikeeEvent()
    object Empty : LikeeEvent()
}