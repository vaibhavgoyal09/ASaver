package com.mystikcoder.statussaver.events.facebook

import com.mystikcoder.statussaver.model.facebook.FacebookNode

sealed class UsersDataEvent {
    class Success(val list: ArrayList<FacebookNode>?) : UsersDataEvent()
    class Failure(val errorText: String) : UsersDataEvent()
    object Empty : UsersDataEvent()
    object Loading : UsersDataEvent()
}