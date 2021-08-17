package com.mystikcoder.statussaver.presentation.ui.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mystikcoder.statussaver.data.repository.sharechat.abstraction.ShareChatDownloadRepository
import com.mystikcoder.statussaver.extensions.getFileName
import com.mystikcoder.statussaver.presentation.framework.events.common.DownloadRequestEvent
import com.mystikcoder.statussaver.presentation.utils.NetworkState
import com.mystikcoder.statussaver.presentation.utils.SHARE_CHAT
import com.mystikcoder.statussaver.presentation.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareChatViewModel @Inject constructor(
    private val app: Application,
    private val repository: ShareChatDownloadRepository
) : AndroidViewModel(app) {

    private val _stateEvent = MutableStateFlow<DownloadRequestEvent>(DownloadRequestEvent.Idle)
    val downloadEvent: StateFlow<DownloadRequestEvent> = _stateEvent

    fun download(url: String) {

        when {
            url.isEmpty() -> {
                _stateEvent.value = DownloadRequestEvent.Error("Empty Url")
                return
            }
            !url.contains("sharechat") || !Patterns.WEB_URL.matcher(url).matches() -> {
                _stateEvent.value = DownloadRequestEvent.Error("Enter Valid Url")
                return
            }
            !NetworkState.isNetworkAvailable() -> {
                _stateEvent.value = DownloadRequestEvent.Error("No Internet Connection")
                return
            }
        }

        _stateEvent.value = DownloadRequestEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val response = repository.downloadShareChatFile(url)

            if (response.isSuccess) {

                val downloadUrl = response.downloadLink!!

                Utils.startDownload(
                    downloadUrl, Utils.ROOT_DIRECTORY_SHARECHAT, app, downloadUrl.getFileName(
                        SHARE_CHAT
                    )
                )

                _stateEvent.value = DownloadRequestEvent.Success

            } else {
                _stateEvent.value = DownloadRequestEvent.Error(response.errorMessage!!)
            }
        }
    }
}