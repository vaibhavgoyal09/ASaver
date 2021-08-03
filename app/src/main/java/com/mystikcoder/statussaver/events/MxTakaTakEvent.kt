package com.mystikcoder.statussaver.events

sealed class MxTakaTakEvent {
    class Success(val fileName: String, val mediaUrl: String) : MxTakaTakEvent()
    class Failure(val errorText: String) : MxTakaTakEvent()
    object Loading : MxTakaTakEvent()
    object Empty : MxTakaTakEvent()
}