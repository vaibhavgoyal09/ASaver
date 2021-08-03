package com.mystikcoder.statussaver.events

sealed class RopossoEvent {
    class Success(val fileName: String, val videoUrl: String) : RopossoEvent()
    class Failure(val errorText: String) : RopossoEvent()
    object Empty : RopossoEvent()
}
