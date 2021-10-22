package com.mystikcoder.statussaver.framework.events.common

sealed class DownloadRequestEvent{
    object Loading: DownloadRequestEvent()
    object Success: DownloadRequestEvent()
    object Idle: DownloadRequestEvent()
    class Error(val errorMessage: String): DownloadRequestEvent()
}
