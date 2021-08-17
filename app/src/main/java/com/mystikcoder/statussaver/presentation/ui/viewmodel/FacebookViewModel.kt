package com.mystikcoder.statussaver.presentation.ui.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mystikcoder.statussaver.data.repository.facebook.abstraction.FacebookRepository
import com.mystikcoder.statussaver.extensions.getFileName
import com.mystikcoder.statussaver.presentation.framework.events.common.DownloadRequestEvent
import com.mystikcoder.statussaver.presentation.framework.events.facebook.FacebookUserEvent
import com.mystikcoder.statussaver.presentation.framework.events.facebook.FacebookUserStoriesEvent
import com.mystikcoder.statussaver.presentation.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FacebookViewModel @Inject constructor(
    private val app: Application,
    private val preferences: Preferences,
    private val facebookRepository: FacebookRepository
) : AndroidViewModel(app) {

    private val _downloadState = MutableStateFlow<DownloadRequestEvent>(DownloadRequestEvent.Idle)
    val downloadState: StateFlow<DownloadRequestEvent> = _downloadState

    private val _userState = MutableStateFlow<FacebookUserEvent>(FacebookUserEvent.Idle)
    val userState: StateFlow<FacebookUserEvent> = _userState

    private val _userStoriesState =
        MutableStateFlow<FacebookUserStoriesEvent>(FacebookUserStoriesEvent.Idle)
    val userStoriesState: StateFlow<FacebookUserStoriesEvent> = _userStoriesState



    fun downloadFile(url: String) {

        when {
            url.isEmpty() -> {
                _downloadState.value = DownloadRequestEvent.Error("Empty Url")
                return
            }
            !url.contains("facebook") || !url.contains("fb")
                    || !Patterns.WEB_URL.matcher(url).matches() -> {
                _downloadState.value = DownloadRequestEvent.Error("Enter Valid Url")
                return
            }
        }

        _downloadState.value = DownloadRequestEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val response = facebookRepository.downloadFacebookFile(url)

            if (response.isSuccess) {

                val downloadUrl = response.downloadLink!!

                Utils.startDownload(
                    downloadUrl, Utils.ROOT_DIRECTORY_FACEBOOK, app, downloadUrl.getFileName(
                        FACEBOOK
                    )
                )

            } else {
                _downloadState.value = DownloadRequestEvent.Error(response.errorMessage!!)
            }
        }
    }

    fun getUsers() {

        _userState.value = FacebookUserEvent.Loading

        if (!NetworkState.isNetworkAvailable()) {
            _userState.value = FacebookUserEvent.Failure("No Internet Connection")
            return
        }

        val cookies = preferences.getString(FB_COOKIES)
        val fbKey = preferences.getString(FB_KEY)

        if (cookies != null && fbKey != null) {
            CoroutineScope(Dispatchers.IO).launch {

                when (val response = facebookRepository.getUsers(cookies, fbKey)) {
                    is Resource.Success -> {
                        _userState.value = FacebookUserEvent.Success(response.data)
                    }
                    is Resource.Error -> {
                        _userState.value =
                            FacebookUserEvent.Failure(response.throwable ?: "Something Went Wrong")
                    }
                }
            }
        } else {
            _userState.value = FacebookUserEvent.Failure("Not Logged In")
        }
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean(IS_FB_LOGGED_IN)
    }

    fun logOut() {
        preferences.clearFacebookPrefs()
    }

    fun getUserStories(userId: String) {

        _userStoriesState.value = FacebookUserStoriesEvent.Loading

        if (!NetworkState.isNetworkAvailable()) {
            _userStoriesState.value = FacebookUserStoriesEvent.Failure("No Internet Connection")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {

            val cookies = preferences.getString(FB_COOKIES)
            val fbKey = preferences.getString(FB_KEY)

            if (cookies != null && fbKey != null) {

                when (val response = facebookRepository.getUserStories(cookies, fbKey, userId)) {

                    is Resource.Success -> {
                        _userStoriesState.value = FacebookUserStoriesEvent.Success(response.data)
                    }
                    is Resource.Error -> {
                        _userStoriesState.value = FacebookUserStoriesEvent.Failure(
                            response.throwable ?: "Something Went Wrong"
                        )
                    }
                }
            }else{
                _userStoriesState.value = FacebookUserStoriesEvent.Failure("Not Logged In")
            }
        }
    }
}