package com.mystikcoder.statussaver.utils

sealed class Resource<T>(
    val data: T?,
    val throwable: String?,
) {
    class Error<T>(message: String) : Resource<T>(null, message)
    class Success<T>(data: T) : Resource<T>(data, null)
}
