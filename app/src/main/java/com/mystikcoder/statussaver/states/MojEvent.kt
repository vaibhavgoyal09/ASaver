package com.mystikcoder.statussaver.states

sealed class MojEvent {
    class Success(val fileName: String, val mediaUrl: String) : MojEvent()
    class Failure(val errorText: String) : MojEvent()
    object Loading : MojEvent()
    object Empty : MojEvent()
}