package com.mystikcoder.statussaver.framework.presentation.ui.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mystikcoder.statussaver.core.data.repository.josh.abstraction.JoshDownloadRepository
import com.mystikcoder.statussaver.framework.events.common.DownloadRequestEvent
import com.mystikcoder.statussaver.framework.extensions.getFileName
import com.mystikcoder.statussaver.framework.utils.JOSH
import com.mystikcoder.statussaver.framework.utils.NetworkState
import com.mystikcoder.statussaver.framework.videoconverter.PlaylistDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoshViewModel @Inject constructor(
    private val app: Application,
    private val repository: JoshDownloadRepository
) : AndroidViewModel(app) {

    private val _stateEvent = MutableStateFlow<DownloadRequestEvent>(DownloadRequestEvent.Idle)
    val downloadEvent: StateFlow<DownloadRequestEvent> = _stateEvent

    fun download(url: String) {

        when {
            url.isEmpty() -> {
                _stateEvent.value = DownloadRequestEvent.Error("Empty Url")
                return
            }
            !url.contains("myjosh") || !Patterns.WEB_URL.matcher(url).matches() -> {
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

            val response = repository.downloadJoshFile(url)

            if (response.isSuccess) {

                val downloadUrl = response.downloadLink!!

                PlaylistDownloader(downloadUrl, downloadUrl.getFileName(JOSH), app).download()

                _stateEvent.value = DownloadRequestEvent.Success

            } else {
                _stateEvent.value = DownloadRequestEvent.Error(response.errorMessage!!)
            }
        }
    }
}