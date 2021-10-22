package com.mystikcoder.statussaver.framework.presentation.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mystikcoder.statussaver.core.data.repository.chingari.abstraction.ChingariDownloadRepository
import com.mystikcoder.statussaver.core.data.repository.instagram.abstraction.InstagramRepository
import com.mystikcoder.statussaver.core.data.repository.josh.abstraction.JoshDownloadRepository
import com.mystikcoder.statussaver.core.data.repository.likee.abstraction.LikeeDownloadRepository
import com.mystikcoder.statussaver.core.data.repository.mitron.abstraction.MitronDownloadRepository
import com.mystikcoder.statussaver.core.data.repository.moj.abstraction.MojDownloadRepository
import com.mystikcoder.statussaver.core.data.repository.mxtakatak.abstraction.MxTakaTakDownloadRepository
import com.mystikcoder.statussaver.core.data.repository.roposo.abstraction.RoposoDownloadRepository
import com.mystikcoder.statussaver.core.data.repository.sharechat.abstraction.ShareChatDownloadRepository
import com.mystikcoder.statussaver.core.data.repository.tiktok.abstraction.TiktokDownloadRepository
import com.mystikcoder.statussaver.core.data.repository.twitter.abstraction.TwitterDownloadRepository
import com.mystikcoder.statussaver.framework.events.common.DownloadRequestEvent
import com.mystikcoder.statussaver.framework.extensions.getFileName
import com.mystikcoder.statussaver.framework.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val app: Application,
    private val instagramRepository: InstagramRepository,
    private val chingariRepository: ChingariDownloadRepository,
    private val roposoRepository: RoposoDownloadRepository,
    private val tiktokRepository: TiktokDownloadRepository,
    private val likeeRepository: LikeeDownloadRepository,
    private val joshRepository: JoshDownloadRepository,
    private val shareChatRepository: ShareChatDownloadRepository,
    private val mitronRepository: MitronDownloadRepository,
    private val mojRepository: MojDownloadRepository,
    private val mxTakaTakRepository: MxTakaTakDownloadRepository,
    private val twitterRepository: TwitterDownloadRepository,
    private val preferences: Preferences
) : AndroidViewModel(app) {

    private val _stateEvent = MutableStateFlow<DownloadRequestEvent>(DownloadRequestEvent.Idle)
    val downloadRequestState: StateFlow<DownloadRequestEvent> = _stateEvent

//    fun downloadFacebook(url: String) {
//
//        _stateEvent.value = DownloadRequestEvent.Loading
//
//        viewModelScope.launch(Dispatchers.IO) {
//            val response = facebookRepository.downloadFacebookFile(url)
//
//            if (response.isSuccess) {
//                Utils.startDownload(
//                    response.downloadLink!!,
//                    Utils.ROOT_DIRECTORY_FACEBOOK,
//                    app,
//                    response.downloadLink!!.getFileName(
//                        FACEBOOK
//                    )
//                )
//                _stateEvent.value = DownloadRequestEvent.Success
//            } else {
//                _stateEvent.value = DownloadRequestEvent.Error(response.errorMessage!!)
//            }
//        }
//    }

    fun downloadInstagram(url: String) {

        _stateEvent.value = DownloadRequestEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val cookies = "ds_user_id=${preferences.getString(USER_ID)}; sessionid=${
                preferences.getString(
                    SESSION_ID
                )
            }"

            val link = "${getUrlWithoutParameters(url)}$INSTAGRAM_PARAMATERS"

            val response = instagramRepository.downloadInstagramFile(link, cookies)

            if (response.isSuccess) {

                val downloadUrls = response.downloadUrls!!

                for (downloadUrl in downloadUrls) {
                    Utils.startDownload(
                        downloadUrl, Utils.DIRECTORY_INSTAGRAM, app, downloadUrl.getFileName(
                            INSTAGRAM
                        )
                    )
                }
                _stateEvent.value = DownloadRequestEvent.Success

            } else {
                _stateEvent.value = DownloadRequestEvent.Error(response.errorMessage!!)
            }
        }
    }

    fun downloadMoj(url: String) {
        _stateEvent.value = DownloadRequestEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val response = mojRepository.downloadMojFile(url)

            if (response.isSuccess) {

                Utils.startDownload(
                    response.downloadLink!!,
                    Utils.DIRECTORY_MOJ,
                    app,
                    response.downloadLink!!.getFileName(
                        MOJ
                    )
                )

                _stateEvent.value = DownloadRequestEvent.Success
            } else {
                _stateEvent.value = DownloadRequestEvent.Error(response.errorMessage!!)
            }
        }
    }

    fun downloadMitron(url: String) {

        _stateEvent.value = DownloadRequestEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val response = mitronRepository.downloadMitronFile(url)

            if (response.isSuccess) {
                val downloadLink = response.downloadLink!!
                Utils.startDownload(
                    downloadLink, Utils.DIRECTORY_MITRON, app, downloadLink.getFileName(
                        MITRON
                    )
                )
                _stateEvent.value = DownloadRequestEvent.Success
            } else {
                _stateEvent.value = DownloadRequestEvent.Error(response.errorMessage!!)
            }
        }
    }

    fun downloadTikTok(url: String) {

        _stateEvent.value = DownloadRequestEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val response = tiktokRepository.downloadTiktokFile(url)

            if (response.isSuccess) {
                val downloadUrl = response.downloadLink!!
                Utils.startDownload(
                    downloadUrl, Utils.DIRECTORY_TIK_TOK, app, downloadUrl.getFileName(
                        TIKTOK
                    )
                )
                _stateEvent.value = DownloadRequestEvent.Success
            } else {
                _stateEvent.value = DownloadRequestEvent.Error(response.errorMessage!!)
            }
        }
    }

    fun downloadChingari(url: String) {

        _stateEvent.value = DownloadRequestEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val response = chingariRepository.downloadChingariFile(url)

            if (response.isSuccess) {
                val downloadUrl = response.downloadLink!!

                Utils.startDownload(
                    downloadUrl, Utils.DIRECTORY_CHINGARI, app, downloadUrl.getFileName(
                        CHINGARI
                    )
                )
                _stateEvent.value = DownloadRequestEvent.Success

            } else {
                _stateEvent.value = DownloadRequestEvent.Error(response.errorMessage!!)
            }
        }
    }

    fun downloadRoposo(url: String) {

        _stateEvent.value = DownloadRequestEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val response = roposoRepository.downloadRoposeFile(url)

            if (response.isSuccess) {
                val downloadUrl = response.downloadLink!!

                Utils.startDownload(
                    downloadUrl, Utils.DIRECTORY_ROPOSSO, app, downloadUrl.getFileName(
                        ROPOSO
                    )
                )
                _stateEvent.value = DownloadRequestEvent.Success
            } else {
                _stateEvent.value = DownloadRequestEvent.Error(response.errorMessage!!)
            }
        }
    }

    fun downloadLikee(url: String) {

        _stateEvent.value = DownloadRequestEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val response = likeeRepository.downloadLikeeFile(url)

            if (response.isSuccess) {

                val downloadUrl = response.downloadLink!!

                Utils.startDownload(
                    downloadUrl,
                    Utils.DIRECTORY_LIKEE,
                    app,
                    downloadUrl.getFileName(LIKEE)
                )
                _stateEvent.value = DownloadRequestEvent.Success
            } else {
                _stateEvent.value = DownloadRequestEvent.Error(response.errorMessage!!)
            }
        }
    }

    fun downloadJosh(url: String) {

        _stateEvent.value = DownloadRequestEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val response = joshRepository.downloadJoshFile(url)

            if (response.isSuccess) {

                val downloadUrl = response.downloadLink!!

                Utils.startDownload(
                    downloadUrl, Utils.DIRECTORY_JOSH, app, downloadUrl.getFileName(
                        JOSH
                    )
                )
                _stateEvent.value = DownloadRequestEvent.Success
            }else {
                _stateEvent.value = DownloadRequestEvent.Error(response.errorMessage!!)
            }
        }
    }

    fun downloadTwitter(url: String) {

        _stateEvent.value = DownloadRequestEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val response = twitterRepository.downloadTwitterFile(url)

            if (response.isSuccess) {

                val downloadUrl = response.downloadLink!!

                Utils.startDownload(
                    downloadUrl, Utils.DIRECTORY_TWITTER, app, downloadUrl.getFileName(
                        TWITTER
                    )
                )
                _stateEvent.value = DownloadRequestEvent.Success
            }else {
                _stateEvent.value = DownloadRequestEvent.Error(response.errorMessage!!)
            }
        }
    }

    fun downloadShareChat(url: String) {

        _stateEvent.value = DownloadRequestEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val response = shareChatRepository.downloadShareChatFile(url)

            if (response.isSuccess) {

                val downloadUrl = response.downloadLink!!

                Utils.startDownload(
                    downloadUrl, Utils.DIRECTORY_SHARECHAT, app, downloadUrl.getFileName(
                        SHARE_CHAT
                    )
                )
                _stateEvent.value = DownloadRequestEvent.Success
            }else {
                _stateEvent.value = DownloadRequestEvent.Error(response.errorMessage!!)
            }
        }
    }

    fun downloadMxTakaTak(url: String) {

        _stateEvent.value = DownloadRequestEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val response = mxTakaTakRepository.downloadMxTakaTakFile(url)

            if (response.isSuccess) {

                val downloadUrl = response.downloadLink!!

                Utils.startDownload(
                    downloadUrl, Utils.DIRECTORY_MX_TAKA_TAK, app, downloadUrl.getFileName(
                        MX_TAKA_TAK
                    )
                )
                _stateEvent.value = DownloadRequestEvent.Success
            }else {
                _stateEvent.value = DownloadRequestEvent.Error(response.errorMessage!!)
            }
        }
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
