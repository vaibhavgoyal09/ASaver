package com.mystikcoder.statussaver.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.mystikcoder.statussaver.model.facebook.FacebookEdges
import com.mystikcoder.statussaver.model.facebook.FacebookNode
import com.mystikcoder.statussaver.model.instagram.*
import com.mystikcoder.statussaver.model.mxtakatak.MxTakaTak
import com.mystikcoder.statussaver.model.twitter.TwitterResponse
import com.mystikcoder.statussaver.repository.instagram.CallResultInterface
import com.mystikcoder.statussaver.repository.instagram.FullDetailsInfoApiInterface
import com.mystikcoder.statussaver.repository.instagram.StoriesApiInterface
import com.mystikcoder.statussaver.repository.mxtakatak.CallMxTakaTakInterface
import com.mystikcoder.statussaver.repository.twitter.CallTwitterInterface
import com.mystikcoder.statussaver.utils.Resource
import com.mystikcoder.statussaver.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.helper.HttpConnection
import org.jsoup.nodes.Document
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val callResultRepository: CallResultInterface,
    private val storiesRepository: StoriesApiInterface,
    private val fullDetailsRepository: FullDetailsInfoApiInterface,
    private val callMxTakaTakRepository: CallMxTakaTakInterface,
    private val callTwitterRepository: CallTwitterInterface
) : ViewModel() {

    //region:: ChingariViewModel
    sealed class ChingariEvent {
        class Success(val fileName: String, val videoUrl: String) : ChingariEvent()
        class Failure(val errorText: String) : ChingariEvent()
        object Empty : ChingariEvent()
    }

    private val _chingariData = MutableStateFlow<ChingariEvent>(ChingariEvent.Empty)
    val chingariData: StateFlow<ChingariEvent> = _chingariData

    fun getChingariData(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {

                Log.e("Chingari Data called", url)

                val chingariDocument: Document = Jsoup.connect(url).get()

                val videoUrl =
                    chingariDocument.select("meta[property=\"og:video:secure_url\"]")
                        .last()
                        .attr("content")
                if (!videoUrl.isNullOrEmpty()) {
                    _chingariData.value =
                        ChingariEvent.Success(getFileName(videoUrl), videoUrl)
                }
            }.getOrElse {
                _chingariData.value = ChingariEvent.Failure(it.message ?: "An error occurred")
            }
        }
    }
    //endregion

    //region:: FacebookViewModel
    sealed class FacebookEvent {
        class Success(val fileName: String, val videoUrl: String) : FacebookEvent()
        class Failure(val errorText: String) : FacebookEvent()
        object Empty : FacebookEvent()
    }

    sealed class UsersDataEvent {
        class Success(val list: ArrayList<FacebookNode>?) : UsersDataEvent()
        class Failure(val errorText: String) : UsersDataEvent()
        object Empty : UsersDataEvent()
        object Loading : UsersDataEvent()
    }

    sealed class StoriesDataEvent {
        class Success(val list: ArrayList<FacebookNode>?) : StoriesDataEvent()
        class Failure(val errorText: String) : StoriesDataEvent()
        object Empty : StoriesDataEvent()
    }

    private val _facebookData = MutableStateFlow<FacebookEvent>(FacebookEvent.Empty)
    val facebookData: StateFlow<FacebookEvent> = _facebookData

    private var edgeModelList: ArrayList<FacebookNode>? = null
    private val _usersDataList = MutableStateFlow<UsersDataEvent>(UsersDataEvent.Empty)
    val usersDataList: StateFlow<UsersDataEvent> = _usersDataList

    private val _facebookStoriesData = MutableStateFlow<StoriesDataEvent>(StoriesDataEvent.Empty)
    val facebookStoriesData: StateFlow<StoriesDataEvent> = _facebookStoriesData

    fun getFacebookData(url: String) {

        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                Log.e("Facebook Data called", url)

                val facebookDocument: Document = Jsoup.connect(url).get()

                Log.e("FacebookDocument", facebookDocument.toString())

                Log.e(
                    "Video url",
                    facebookDocument.select("meta[property=\"og:video:url\"]")
                        .last()
                        .attr("content")
                )

                val videoUrl =
                    facebookDocument.select("meta[property=\"og:video:url\"]")
                        .last()
                        .attr("content")

                Log.e("VideoUrl", videoUrl)

                if (!videoUrl.isNullOrEmpty()) {
                    _facebookData.value =
                        FacebookEvent.Success(getFileName(videoUrl), videoUrl)
                } else {
                    _facebookData.value = FacebookEvent.Failure("An error occurred")
                }

            }.getOrElse {
                _facebookData.value =
                    FacebookEvent.Failure(it.message ?: "An error occurred")
            }
        }
    }

    fun getFacebookUserData(fbCookies: String, fbKey: String) {

        edgeModelList = ArrayList()

        _usersDataList.value = UsersDataEvent.Loading

        AndroidNetworking
            .post("https://www.facebook.com/api/graphql/")
            .addHeaders("accept-language", "en,en-US;q=0.9,fr;q=0.8,ar;q=0.7")
            .addHeaders("cookie", fbCookies)
            .addHeaders(
                "user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36"
            )
            .addHeaders(HttpConnection.CONTENT_TYPE, "application/json")
            .addBodyParameter("fb_dtsg", fbKey)
            .addBodyParameter(
                "variables",
                "{\"bucketsCount\":200,\"initialBucketID\":null,\"pinnedIDs\":[\"\"],\"scale\":3}"
            )
            .addBodyParameter("doc_id", "2893638314007950")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    println("json object :-" + response.toString())

                    response?.let {
                        try {
                            val tempResponse = it
                                .getJSONObject("data")
                                .getJSONObject("me")
                                .getJSONObject("unified_stories_buckets")

                            val edges: FacebookEdges = Gson()
                                .fromJson(
                                    tempResponse.toString(),
                                    object : TypeToken<FacebookEdges>() {}.type
                                ) as FacebookEdges

                            if (edges.edgesModel.size > 0) {
                                edgeModelList?.clear()
                                edgeModelList?.addAll(edges.edgesModel)
                                _usersDataList.value = UsersDataEvent.Success(edgeModelList)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            _usersDataList.value =
                                UsersDataEvent.Failure(e.message ?: "An error occurred")
                        }
                    }
                }

                override fun onError(anError: ANError?) {
                    _usersDataList.value =
                        UsersDataEvent.Failure(anError?.errorBody ?: "An error occurred")
                }

            })
    }

    fun getStories(fbCookies: String, fbKey: String, aString: String) {
        AndroidNetworking
            .post("https://www.facebook.com/api/graphql/")
            .addHeaders("accept-language", "en,en-US;q=0.9,fr;q=0.8,ar;q=0.7")
            .addHeaders("cookies", fbCookies)
            .addHeaders(
                "user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36"
            )
            .addHeaders(HttpConnection.CONTENT_TYPE, "application/json")
            .addBodyParameter(
                "fb_dtsg", fbKey
            ).also {
                it.addBodyParameter(
                    "variables",
                    "{\"bucketID\":\"" +
                            aString +
                            "\",\"initialBucketID\":\"" +
                            aString +
                            "\",\"initialLoad\":false,\"scale\":5}"
                )
                    .addBodyParameter("doc_id", "2558148157622405")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            println("json object :-" + response.toString())
                            response?.let { responseObject ->
                                try {

                                    val tempJson = responseObject
                                        .getJSONObject("data")
                                        .getJSONObject("bucket")
                                        .getJSONObject("unified_stories")

                                    val edgeModel = Gson().fromJson(
                                        tempJson.toString(),
                                        object : TypeToken<FacebookEdges>() {}.type
                                    ) as FacebookEdges

                                    edgeModel.edgesModel[0].nodeData.attachmentsList

                                    _facebookStoriesData.value =
                                        StoriesDataEvent.Success(edgeModel.edgesModel)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    _facebookStoriesData.value =
                                        StoriesDataEvent.Failure("No story data found")
                                }
                            }
                        }

                        override fun onError(anError: ANError?) {
                            _facebookStoriesData.value =
                                StoriesDataEvent.Failure(
                                    anError?.errorBody ?: "An error occurred"
                                )
                        }
                    })
            }
    }

    //endregion

    //region:: InstagramViewModel
    sealed class InstagramStoryDetailEvent {
        class Success(val data: ArrayList<ItemModel>?) : InstagramStoryDetailEvent()
        class Failure(val errorText: String) : InstagramStoryDetailEvent()
        object Loading : InstagramStoryDetailEvent()
        object Empty : InstagramStoryDetailEvent()
    }

    sealed class InstagramStoryEvent {
        class Success(val data: ArrayList<TrayModel>?) : InstagramStoryEvent()
        class Failure(val errorText: String) : InstagramStoryEvent()
        object Loading : InstagramStoryEvent()
        object Empty : InstagramStoryEvent()
    }

    sealed class InstagramEvent {
        class Success(val mediaUrl: ArrayList<String>?) : InstagramEvent()
        class Failure(val errorText: String) : InstagramEvent()
        object Loading : InstagramEvent()
        object Empty : InstagramEvent()
    }

    private val _instagramData = MutableStateFlow<InstagramEvent>(InstagramEvent.Empty)
    private val _instagramStoriesData =
        MutableStateFlow<InstagramStoryEvent>(InstagramStoryEvent.Empty)
    private val _instagramStoriesDetailsData =
        MutableStateFlow<InstagramStoryDetailEvent>(InstagramStoryDetailEvent.Empty)
    val instagramData: StateFlow<InstagramEvent> = _instagramData
    val instagramStoriesData: StateFlow<InstagramStoryEvent> = _instagramStoriesData
    val instagramStoriesDetailsData: StateFlow<InstagramStoryDetailEvent> =
        _instagramStoriesDetailsData
    private lateinit var callResultResponse: Resource<JsonObject>
    private lateinit var storiesResponse: Resource<StoryModel>
    private lateinit var storiesDetailResponse: Resource<FullDetailModel>

    fun getCallResultData(url: String, cookie: String?) {

        _instagramData.value = InstagramEvent.Loading

        val downloadUrls = ArrayList<String>()

        viewModelScope.launch(Dispatchers.IO) {

            Log.e("Instagram Data called", url)

            Log.e("TAG", url)
            Log.e("TAG", "get Response called")

            callResultResponse =
                if (Utils.isNullOrEmpty(cookie)) {
                    callResultRepository.callResult(
                        url,
                        "",
                        "Instagram 9.5.2 (iPhone7,2; iPhone OS 9_3_3; en_US; en-US; scale=2.00; 750x1334) AppleWebKit/420+"
                    )
                } else {
                    callResultRepository.callResult(
                        url,
                        cookie!!,
                        "Instagram 9.5.2 (iPhone7,2; iPhone OS 9_3_3; en_US; en-US; scale=2.00; 750x1334) AppleWebKit/420+"
                    )
                }
            when (callResultResponse) {
                is Resource.Success -> {
                    try {

                        Log.e("Call Response Data", callResultResponse.data.toString())

                        val responseModel: ResponseModel =
                            Gson().fromJson(
                                callResultResponse.data.toString(),
                                object : TypeToken<ResponseModel>() {}.type
                            ) as ResponseModel

                        Log.e("Response Model", responseModel.toString())

                        val edgeSidecarToChildren: EdgeSidecarToChildren? =
                            responseModel.graphQl.shortcodeMedia.edgeSidecarToChildren

                        Log.e(
                            "EdgeSideCarToChildren",
                            edgeSidecarToChildren?.toString() ?: "Null"
                        )

                        var mediaUrl: String

                        if (edgeSidecarToChildren != null) {

                            val edgeArrayList: List<Edge> = edgeSidecarToChildren.edges

                            for (edge in edgeArrayList) {
                                if (edge.node.isVideo) {
                                    mediaUrl = edge.node.videoUrl
                                    downloadUrls.add(mediaUrl)
                                } else {
                                    mediaUrl =
                                        edge.node.displayResources[2].src
                                    downloadUrls.add(mediaUrl)
                                }
                            }
                        } else {
                            if (responseModel.graphQl.shortcodeMedia.isVideo) {
                                mediaUrl = responseModel.graphQl.shortcodeMedia.videoUrl

                                downloadUrls.add(mediaUrl)

                            } else {
                                mediaUrl =
                                    responseModel.graphQl.shortcodeMedia.displayResources[2].src

                                downloadUrls.add(mediaUrl)
                            }
                        }

                        _instagramData.value = InstagramEvent.Success(downloadUrls)

                    } catch (e: Exception) {
                        Log.e("TAG, ", e.message ?: "An error occurred")
                        _instagramData.value =
                            InstagramEvent.Failure(e.message ?: "An error occurred")
                    }
                }
                is Resource.Error -> {
                    _instagramData.value =
                        InstagramEvent.Failure(callResultResponse.throwable!!)
                }
            }
        }
    }

    fun getStories(cookie: String?) {
        _instagramStoriesData.value = InstagramStoryEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {
            storiesResponse = if (Utils.isNullOrEmpty(cookie)) {
                storiesRepository.getStoriesApi(
                    "https://i.instagram.com/api/v1/feed/reels_tray/",
                    "",
                    "\"Instagram 9.5.2 (iPhone7,2; iPhone OS 9_3_3; en_US; en-US; scale=2.00; 750x1334) AppleWebKit/420+\""
                )
            } else {
                storiesRepository.getStoriesApi(
                    "https://i.instagram.com/api/v1/feed/reels_tray/",
                    cookie!!,
                    "\"Instagram 9.5.2 (iPhone7,2; iPhone OS 9_3_3; en_US; en-US; scale=2.00; 750x1334) AppleWebKit/420+\""
                )
            }
            when (storiesResponse) {
                is Resource.Success -> {
                    _instagramStoriesData.value =
                        InstagramStoryEvent.Success(storiesResponse.data?.tray)
                }
                is Resource.Error -> {
                    _instagramStoriesData.value =
                        InstagramStoryEvent.Failure(storiesResponse.throwable!!)
                }
            }
        }
    }

    fun getStoryDetails(cookie: String, userId: String) {

        viewModelScope.launch(Dispatchers.IO) {
            storiesDetailResponse = fullDetailsRepository.getFullDetailInfoApi(
                "https://i.instagram.com/api/v1/users/$userId/full_detail_info?max_id=",
                cookie,
                "\"Instagram 9.5.2 (iPhone7,2; iPhone OS 9_3_3; en_US; en-US; scale=2.00; 750x1334) AppleWebKit/420+\""
            )
            when (storiesDetailResponse) {
                is Resource.Success -> {
                    _instagramStoriesDetailsData.value =
                        InstagramStoryDetailEvent.Success(storiesDetailResponse.data?.reelsFeed?.items)
                }
                is Resource.Error -> {
                    _instagramStoriesDetailsData.value =
                        InstagramStoryDetailEvent.Failure(storiesDetailResponse.throwable!!)
                }
            }
        }
    }
    //endregion

    //region:: JoshViewModel
    sealed class JoshEvent {
        class Success(val fileName: String, val videoUrl: String) : JoshEvent()
        class Failure(val errorText: String) : JoshEvent()
        object Empty : JoshEvent()
    }

    private val _joshData = MutableStateFlow<JoshEvent>(JoshEvent.Empty)
    val joshData: StateFlow<JoshEvent> = _joshData

    fun getJoshData(url: String) {

        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val joshDocument: Document = Jsoup.connect(url).get()
                val html =
                    joshDocument.select("script[id=\"__NEXT_DATA__\"]")
                        .last()
                        .html()

                if (!html.isNullOrEmpty()) {

                    val videoUrl =
                        (JSONObject(html).getJSONObject("props").getJSONObject("pageProps")
                            .getJSONObject("detail").getJSONObject("data")
                            .getString("m3u8_url")).toString()

                    _joshData.value =
                        JoshEvent.Success(
                            "josh_" + System.currentTimeMillis().toString() + ".mp4",
                            videoUrl
                        )
                }
            }.getOrElse {
                _joshData.value = JoshEvent.Failure(it.message ?: "An error occurred")
            }
        }
    }
    //endregion

    //region:: LikeeViewModel
    sealed class LikeeEvent {
        class Success(val videoUrl: String, val fileName: String) : LikeeEvent()
        class Failure(val errorText: String) : LikeeEvent()
        object Empty : LikeeEvent()
    }

    private val _likeeData = MutableStateFlow<LikeeEvent>(LikeeEvent.Empty)
    val likeeData: StateFlow<LikeeEvent> = _likeeData

    private var pattern =
        Pattern.compile("window\\.data \\s*=\\s*(\\{.+?\\});") // Do not remove back slashes

    fun getLikeeData(link: String) {

        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val likeeDocument: Document = Jsoup.connect(link).get()

                var jsonData = ""
                val matcher: Matcher = pattern.matcher(likeeDocument.toString())
                while (matcher.find()) {
                    jsonData = matcher.group().replaceFirst("window.data = ".toRegex(), "")
                        .replace(";", "")
                }

                val jsonObject = JSONObject(jsonData)
                val videoUrl = jsonObject.getString("video_url").replace("_4", "")

                _likeeData.value = LikeeEvent.Success(videoUrl, getFileName(videoUrl))
            }.getOrElse {
                _likeeData.value = LikeeEvent.Failure(it.message ?: "An error occurred")
            }
        }
    }
    //endregion

    //region:: MitronViewModel
    sealed class MitronEvent {
        class Success(val fileName: String, val videoUrl: String) : MitronEvent()
        class Failure(val errorText: String) : MitronEvent()
        object Empty : MitronEvent()
    }

    private val _mitronData = MutableStateFlow<MitronEvent>(MitronEvent.Empty)
    val mitronData: StateFlow<MitronEvent> = _mitronData

    fun getMitronData(url: Array<String>) {

        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val mitronDocument: Document = Jsoup.connect(url[0]).get()
                val html =
                    mitronDocument.select("script[id=\"__NEXT_DATA__\"]")
                        .last()
                        .html()

                if (!html.isNullOrEmpty()) {

                    val videoUrl =
                        (JSONObject(html).getJSONObject("props").getJSONObject("pageProps")
                            .getJSONObject("video").getString("videoUrl")).toString()

                    _mitronData.value =
                        MitronEvent.Success(getFileName(videoUrl), videoUrl)
                }
            }.getOrElse {
                _mitronData.value = MitronEvent.Failure(it.message ?: "An error occurred")
            }
        }
    }
    //endregion

    //region:: MojViewModel
    sealed class MojEvent {
        class Success(val fileName: String, val mediaUrl: String) : MojEvent()
        class Failure(val errorText: String) : MojEvent()
        object Loading : MojEvent()
        object Empty : MojEvent()
    }

    private val _mojData = MutableStateFlow<MojEvent>(MojEvent.Empty)
    val mojData: StateFlow<MojEvent> = _mojData

    fun getMojData(url: String) {

        _mojData.value = MojEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {
            mxTakaTakResponse =
                callMxTakaTakRepository.getTakaTakData(Utils.MX_TAKA_TAK_URL, url)

            when (mxTakaTakResponse) {
                is Resource.Success -> {
                    kotlin.runCatching {
                        mxTakaTakResponse.data?.let {
                            if (it.responseCode == "200") {
                                val mainVideo = it.data.mainVideo
                                _mojData.value = MojEvent.Success(
                                    "moj" + System.currentTimeMillis().toString() + ".mp4",
                                    mainVideo
                                )
                            }
                        }
                    }.getOrElse {
                        _mojData.value =
                            MojEvent.Failure(it.message ?: "An error occurred")
                    }
                }
                is Resource.Error -> {
                    _mojData.value = MojEvent.Failure(mxTakaTakResponse.throwable!!)
                }
            }
        }
    }
    //endregion

    //region:: TikTokViewModel
    sealed class TikTokEvent {
        class Success(val fileName: String, val mediaUrl: String) : TikTokEvent()
        class Failure(val errorText: String) : TikTokEvent()
        object Loading : TikTokEvent()
        object Empty : TikTokEvent()
    }

    private val _tiktokData = MutableStateFlow<TikTokEvent>(TikTokEvent.Empty)
    val tiktikData: StateFlow<TikTokEvent> = _tiktokData

    fun getTikTokData(url: String) {

        _tiktokData.value = TikTokEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {
            mxTakaTakResponse =
                callMxTakaTakRepository.getTakaTakData(Utils.MX_TAKA_TAK_URL, url)

            when (mxTakaTakResponse) {
                is Resource.Success -> {
                    kotlin.runCatching {
                        mxTakaTakResponse.data?.let {
                            if (it.responseCode == "200") {
                                val mainVideo = it.data.mainVideo
                                _mojData.value = MojEvent.Success(
                                    "TikTok" + System.currentTimeMillis().toString() + ".mp4",
                                    mainVideo
                                )
                            }
                        }
                    }.getOrElse {
                        _tiktokData.value =
                            TikTokEvent.Failure(it.message ?: "An error occurred")
                    }
                }
                is Resource.Error -> {
                    _tiktokData.value = TikTokEvent.Failure(mxTakaTakResponse.throwable!!)
                }
            }
        }
    }
    //endregion

    ///region:: MxTakaTakViewModel
    sealed class MxTakaTakEvent {
        class Success(val fileName: String, val mediaUrl: String) : MxTakaTakEvent()
        class Failure(val errorText: String) : MxTakaTakEvent()
        object Loading : MxTakaTakEvent()
        object Empty : MxTakaTakEvent()
    }

    private val _mxTakaTakData = MutableStateFlow<MxTakaTakEvent>(MxTakaTakEvent.Empty)
    val mxTakaTakData: StateFlow<MxTakaTakEvent> = _mxTakaTakData

    private lateinit var mxTakaTakResponse: Resource<MxTakaTak>

    fun getMxTakaTakData(url: String) {

        _mxTakaTakData.value = MxTakaTakEvent.Loading

        viewModelScope.launch(Dispatchers.IO) {
            mxTakaTakResponse =
                callMxTakaTakRepository.getTakaTakData(Utils.MX_TAKA_TAK_URL, url)

            when (mxTakaTakResponse) {
                is Resource.Success -> {
                    kotlin.runCatching {
                        mxTakaTakResponse.data?.let {
                            if (it.responseCode == "200") {
                                val mainVideo = it.data.mainVideo
                                _mxTakaTakData.value = MxTakaTakEvent.Success(
                                    "mx" + System.currentTimeMillis().toString() + ".mp4",
                                    mainVideo
                                )
                            }
                        }
                    }.getOrElse {
                        _mxTakaTakData.value =
                            MxTakaTakEvent.Failure(it.message ?: "An error occurred")
                    }
                }
                is Resource.Error -> {
                    _mxTakaTakData.value = MxTakaTakEvent.Failure(mxTakaTakResponse.throwable!!)
                }
            }
        }
    }
    //endregion

    //region:: RoposoViewModel
    sealed class RopossoEvent {
        class Success(val fileName: String, val videoUrl: String) : RopossoEvent()
        class Failure(val errorText: String) : RopossoEvent()
        object Empty : RopossoEvent()
    }

    private val _roposoData = MutableStateFlow<RopossoEvent>(RopossoEvent.Empty)
    val roposoData: StateFlow<RopossoEvent> = _roposoData

    fun getRopossoData(url: String) {

        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val ropossoDocument: Document = Jsoup.connect(url).get()
                var videoUrl =
                    ropossoDocument.select("meta[property=\"og:video\"]")
                        .last()
                        .attr("content")
                Log.e("VideoUrl", videoUrl)
                if (videoUrl.isNullOrEmpty()) {
                    videoUrl =
                        ropossoDocument.select("meta[property=\"og:video:url\"]")
                            .last()
                            .attr("content")

                    if (!videoUrl.isNullOrEmpty()) {
                        _roposoData.value =
                            RopossoEvent.Success(getFileName(videoUrl), videoUrl)
                    }
                }
            }.getOrElse {
                _roposoData.value = RopossoEvent.Failure(it.message ?: "An error occurred")
            }
        }
    }
    //endregion

    //region:: ShareChatViewModel
    sealed class ShareChatEvent {
        class Success(val fileName: String, val videoUrl: String) : ShareChatEvent()
        class Failure(val errorText: String) : ShareChatEvent()
        object Empty : ShareChatEvent()
    }

    private val _shareChatData = MutableStateFlow<ShareChatEvent>(ShareChatEvent.Empty)
    val shareChatData: StateFlow<ShareChatEvent> = _shareChatData

    fun getShareChatData(url: String) {

        viewModelScope.launch(Dispatchers.IO) {
            val videoUrl = returnShareChatResults(url, "video")
            if (videoUrl != null){
                _shareChatData.value = ShareChatEvent.Success("Share" + System.currentTimeMillis().toString() +".mp4", videoUrl)
            }else{
                val imageUrl = returnShareChatResults(url, "image")
                if (imageUrl != null){
                    _shareChatData.value = ShareChatEvent.Success("Share" + System.currentTimeMillis().toString() +".png", imageUrl)
                }else{
                    _shareChatData.value = ShareChatEvent.Failure("Enter valid Url")
                }
            }
        }
    }

    private fun returnShareChatResults(url: String, type: String): String? {
        val shareChatDocument: Document = Jsoup.connect(url).get()
        return try {
            if (type == "video") {
                shareChatDocument.select("meta[property=\"og:video:secure_url\"]")
                    .last()
                    .attr("content")
            } else {
                shareChatDocument.select("meta[property=\"og:image\"]")
                    .last()
                    .attr("content")
            }
        } catch (e: Exception) {
            return null
        }
    }

    //endregion

    //region:: TwitterViewModel
    sealed class TwitterEvent {
        class Success(val fileName: String, val mediaUrl: String) : TwitterEvent()
        class Failure(val errorText: String) : TwitterEvent()
        object Loading : TwitterEvent()
        object Empty : TwitterEvent()
    }

    private val _twitterData = MutableStateFlow<TwitterEvent>(TwitterEvent.Empty)
    val twitterData: StateFlow<TwitterEvent> = _twitterData

    private lateinit var twitterResponse: Resource<TwitterResponse>

    fun getTwitterData(id: String) {

        _twitterData.value = TwitterEvent.Loading

        viewModelScope.launch {
            val url = "https://twittervideodownloaderpro.com/twittervideodownloadv2/index.php"
            twitterResponse = callTwitterRepository.callTwitter(url, id)

            when (twitterResponse) {
                is Resource.Success -> {
                    kotlin.runCatching {
                        twitterResponse.data?.let {
                            var videoUrl = it.videos[0].url
                            val videoType = it.videos[0].type

                            if (videoType == "image") {

                                _twitterData.value =
                                    TwitterEvent.Success(
                                        getTwitterFileName(videoUrl, "image"),
                                        videoUrl
                                    )

                            } else {
                                videoUrl = it.videos[it.videos.size - 1].url

                                _twitterData.value =
                                    TwitterEvent.Success(
                                        getTwitterFileName(videoUrl, "mp4"),
                                        videoUrl
                                    )

                            }
                        }
                    }.getOrElse {
                        Log.e("error", it.message ?: "No media found on this tweet")
                        _twitterData.value =
                            TwitterEvent.Failure("No media found on this tweet")
                    }
                }
                is Resource.Error -> {
                    _twitterData.value = TwitterEvent.Failure(twitterResponse.throwable!!)
                }
            }
        }
    }

    private fun getTwitterFileName(url: String?, type: String): String {
        return if (type == "image") {
            try {
                File(URL(url).path).name + ""
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                System.currentTimeMillis().toString() + ".jpg"
            }
        } else {
            try {
                File(URL(url).path).name + ""
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                System.currentTimeMillis().toString() + ".mp4"
            }
        }
    }
    //endregion

    private fun getFileName(url: String?): String {
        return try {
            File(URL(url).path).name + ".mp4"
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            System.currentTimeMillis().toString() + ".mp4"
        }
    }

    fun cancelDownload() {
        viewModelScope.cancel()
    }
}
