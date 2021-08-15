package com.mystikcoder.statussaver.presentation.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mystikcoder.statussaver.domain.events.common.DownloadRequestEvent
import com.mystikcoder.statussaver.domain.events.instagram.InstagramUserEvent
import com.mystikcoder.statussaver.domain.events.instagram.InstagramUserStoriesEvent
import com.mystikcoder.statussaver.domain.repository.instagram.abstraction.InstagramRepository
import com.mystikcoder.statussaver.extensions.getFileName
import com.mystikcoder.statussaver.presentation.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class InstagramViewModel @Inject constructor(
    private val app: Application,
    private val preference: Preferences,
    private val repository: InstagramRepository
) : AndroidViewModel(app) {

    private val _downloadEvent =
        MutableStateFlow<DownloadRequestEvent>(DownloadRequestEvent.Idle)
    val downloadEvent: StateFlow<DownloadRequestEvent> = _downloadEvent

    private val _userEvent = MutableStateFlow<InstagramUserEvent>(InstagramUserEvent.Idle)
    val userEvent: StateFlow<InstagramUserEvent> = _userEvent

    private val _userStoriesEvent =
        MutableStateFlow<InstagramUserStoriesEvent>(InstagramUserStoriesEvent.Idle)
    val userStoriesEvent: StateFlow<InstagramUserStoriesEvent> = _userStoriesEvent

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    init {
        _isLoggedIn.value = preference.getBoolean(IS_INSTA_LOGGED_IN)
    }

    fun callDownload(url: String?) {

        when {
            url.isNullOrEmpty() -> {
                _downloadEvent.value = DownloadRequestEvent.Error("Empty Url")
                return
            }
            !Patterns.WEB_URL.matcher(url).matches() || !url.contains("instagram") -> {
                _downloadEvent.value = DownloadRequestEvent.Error("Enter Valid Url")
            }
        }

        _downloadEvent.value = DownloadRequestEvent.Loading

        val link = "${getUrlWithoutParameters(url!!)}$INSTAGRAM_PARAMATERS"

        val cookies = "ds_user_id=${preference.getString(USER_ID)}; sessionid=${
            preference.getString(SESSION_ID)
        }"

        viewModelScope.launch(Dispatchers.IO) {
            val response =
                repository.downloadInstagramFile(link, cookies)

            if (response.isSuccess) {

                val downloadUrls = response.downloadUrls!!

                for (downloadUrl in downloadUrls) {
                    Utils.startDownload(
                        downloadUrl, Utils.ROOT_DIRECTORY_INSTAGRAM, app, downloadUrl.getFileName(
                            INSTAGRAM
                        )
                    )
                }
                _downloadEvent.value = DownloadRequestEvent.Success
            } else {
                _downloadEvent.value =
                    DownloadRequestEvent.Error(response.errorMessage ?: "Something Went Wrong")
            }
        }
    }

    fun getUsers() {
        val cookies = "ds_user_id=${preference.getString(USER_ID)}; sessionid=${
            preference.getString(SESSION_ID)
        }"

        CoroutineScope(Dispatchers.IO).launch {

            _userEvent.value = InstagramUserEvent.Loading

            when (val response = repository.getUsers(cookies)) {
                is Resource.Error -> {
                    _userEvent.value =
                        InstagramUserEvent.Failure(response.throwable ?: "Something Went Wrong")
                }
                is Resource.Success -> {
                    _userEvent.value = InstagramUserEvent.Success(response.data)
                }
            }
        }
    }

    fun getUserStories(otherUserId: String) {
        val cookies = "ds_user_id=${preference.getString(USER_ID)}; sessionid=${
            preference.getString(SESSION_ID)
        }"

        CoroutineScope(Dispatchers.IO).launch {

            when (val response = repository.getUserStories(cookies, otherUserId)) {
                is Resource.Success -> {
                    _userStoriesEvent.value = InstagramUserStoriesEvent.Success(response.data)
                }
                is Resource.Error -> {
                    _userStoriesEvent.value = InstagramUserStoriesEvent.Failure(
                        response.throwable ?: "Something Went Wrong"
                    )
                }
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return isLoggedIn.value!!
    }

    fun logOut() {
        _isLoggedIn.postValue(false)
        preference.putBoolean(IS_INSTA_LOGGED_IN, false)
        preference.clearInstagramPrefs()
    }

    private fun getUrlWithoutParameters(url: String): String {
        return try {
            val uri = URI(url)
            URI(
                uri.scheme,
                uri.authority,
                uri.path,
                null,  // Ignore the query part of the input url
                uri.fragment
            ).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
