package com.mystikcoder.statussaver.states

sealed class MitronEvent {
    class Success(val fileName: String, val videoUrl: String) : MitronEvent()
    class Failure(val errorText: String) : MitronEvent()
    object Empty : MitronEvent()
}