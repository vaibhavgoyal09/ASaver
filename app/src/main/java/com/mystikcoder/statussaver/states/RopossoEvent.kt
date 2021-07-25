package com.mystikcoder.statussaver.states

sealed class RopossoEvent {
    class Success(val fileName: String, val videoUrl: String) : RopossoEvent()
    class Failure(val errorText: String) : RopossoEvent()
    object Empty : RopossoEvent()
}
