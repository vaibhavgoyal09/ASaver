package com.mystikcoder.statussaver.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.util.Patterns
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.background.workers.*
import com.mystikcoder.statussaver.utils.*
import com.mystikcoder.statussaver.videoconverter.PlaylistDownloader
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.util.regex.Matcher
import javax.inject.Inject

@AndroidEntryPoint
class ClipTextObserverService : LifecycleService(), ClipboardManager.OnPrimaryClipChangedListener {

    @Inject
    lateinit var prefManager: PrefManager

    private lateinit var clipboard: ClipboardManager
    private lateinit var notificationManager: NotificationManager
    private val isObserving: MutableLiveData<Boolean> = MutableLiveData()
    private var _isObserving: Boolean = false
    private lateinit var previousCopiedText: String

    companion object {
        val isServiceKilled: MutableLiveData<Boolean> = MutableLiveData()
    }

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    private lateinit var currNotificationBuilder: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        previousCopiedText = ""

        isServiceKilled.value = false

        isObserving.postValue(false)

        isObserving.observe(this) {
            _isObserving = it
            updateNotification(it)
        }
        currNotificationBuilder = baseNotificationBuilder
    }

    private fun startService() {

        isObserving.postValue(true)

        clipboard.addPrimaryClipChangedListener(this)

        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "Service",
                "Service Notification",
                NotificationManager.IMPORTANCE_LOW
            )
            currNotificationBuilder.setChannelId("Service")
            notificationManager.createNotificationChannel(channel)
        }
        startForeground(1, currNotificationBuilder.build())
    }

    private fun updateNotification(isObserving: Boolean) {
        val notificationActionText = if (isObserving) "Pause" else "Resume"
        val isCancellable = !isObserving
        val notificationText =
            if (isObserving) "Automatic Downloading is active" else "Automatic Downloading is paused"

        val pendingIntent: PendingIntent = if (isObserving) {
            PendingIntent.getService(
                this,
                1464,
                Intent(this, ClipTextObserverService::class.java).also {
                    it.action = PAUSE_SERVICE
                }, PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getService(
                this,
                646,
                Intent(this, ClipTextObserverService::class.java).also {
                    it.action = RESUME_SERVICE
                }, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        currNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        currNotificationBuilder.addAction(
            R.drawable.ic_pause,
            notificationActionText,
            pendingIntent
        )
            .addAction(
                R.drawable.ic_cancel,
                "Stop",
                PendingIntent.getService(
                    this,
                    46456,
                    Intent(this, ClipTextObserverService::class.java).also {
                        it.action = STOP_SERVICE
                    }, PendingIntent.FLAG_CANCEL_CURRENT
                )
            )
            .setContentText(notificationText)
            .setAutoCancel(isCancellable)

        notificationManager.notify(1, currNotificationBuilder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let { i ->
            when {
                i.action?.equals(START_SERVICE)!! -> {
                    clipboard.addPrimaryClipChangedListener(this)
                    startService()
                }
                i.action?.equals(PAUSE_SERVICE)!! -> {
                    clipboard.removePrimaryClipChangedListener(this)
                    Log.e("Service", "service paused")
                    isObserving.postValue(false)
                }
                i.action?.equals(STOP_SERVICE)!! -> {
                    clipboard.removePrimaryClipChangedListener(this)
                    isServiceKilled.value = true
                    stopForeground(true)
                    stopSelf()
                }
                i.action?.equals(RESUME_SERVICE)!! -> {
                    clipboard.addPrimaryClipChangedListener(this)
                    isObserving.postValue(true)
                }
                else -> Unit
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onPrimaryClipChanged() {

        if (_isObserving && !isServiceKilled.value!!) {
            if (Utils.isNetworkAvailable(this)) {
                if (Build.VERSION.SDK_INT >= 29) {
                    setupListeners()
                } else {
                    if (Utils.hasWritePermission(this)) {
                        setupListeners()
                    } else {
                        Utils.createToast(this, "Require storage permission")
                    }
                }
            } else {
                Utils.createToast(this, "No Internet connection available")
            }
        }
    }

    private fun setupListeners() {
        val clipText = clipboard.primaryClip?.getItemAt(0)?.text.toString()
        if (clipText == previousCopiedText) {
            return
        } else {
            if (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)!!) {

                when {
                    clipText.contains("instagram") -> {

                        val url = URL(clipText)
                        val host = url.host

                        if (host == "www.instagram.com") {
                            var urlWithoutQP =
                                getUrlWithoutParameters(clipText)

                            urlWithoutQP = "$urlWithoutQP?__a=1"

                            val data = Data.Builder()
                                .putString(WORK_URL, urlWithoutQP)
                                .putString(
                                    WORK_COOKIES,
                                    "ds_user_id=" + prefManager.getString(USER_ID)
                                        .toString() + "; sessionid=" + prefManager.getString(
                                        SESSION_ID
                                    )
                                )
                                .putString(
                                    WORK_USER_AGENT,
                                    "Instagram 9.5.2 (iPhone7,2; iPhone OS 9_3_3; en_US; en-US; scale=2.00; 750x1334) AppleWebKit/420+"
                                )
                                .build()
                            startWorker(data)
                        }
                    }
                    clipText.contains("sharechat") -> {
                        val data =
                            Data.Builder().putString(WORK_URL, extractLinks(clipText))
                                .putString(WORK_PLATFORM_NAME, "sharechat")
                                .build()

                        startShareChatWorker(data, Utils.ROOT_DIRECTORY_SHARECHAT)
                    }
                    clipText.contains("roposo") -> {
                        val data = Data.Builder().putString(WORK_URL, clipText)
                            .putString(WORK_PLATFORM_NAME, "roposo")
                            .build()

                        startShareChatWorker(data, Utils.ROOT_DIRECTORY_ROPOSSO)
                    }
                    clipText.contains("facebook") -> {
                        val data = Data.Builder().putString(WORK_URL, clipText)
                            .putString(WORK_PLATFORM_NAME, "facebook")
                            .build()

                        startShareChatWorker(data, Utils.ROOT_DIRECTORY_FACEBOOK)
                    }
                    clipText.contains("mitron") -> {

                        val split = extractLinks(clipText).split("=")

                        val data = Data.Builder().putString(
                            WORK_URL,
                            arrayOf("https://web.mitron.tv/video/" + split[split.size - 1])[0]
                        )
                            .putString(WORK_PLATFORM_NAME, "mitron")
                            .build()

                        startShareChatWorker(data, Utils.ROOT_DIRECTORY_MITRON)
                    }
                    clipText.contains("chingari") -> {

                        val data =
                            Data.Builder().putString(WORK_URL, extractLinks(clipText))
                                .putString(WORK_PLATFORM_NAME, "chingari")
                                .build()

                        startShareChatWorker(data, Utils.ROOT_DIRECTORY_CHINGARI)
                    }
                    clipText.contains("myjosh") -> {
                        val data =
                            Data.Builder().putString(WORK_URL, extractLinks(clipText))
                                .build()

                        startJoshWorker(data)
                    }
                    clipText.contains("mxtakatak") -> {
                        val data =
                            Data.Builder().putString(WORK_URL, extractLinks(clipText))
                        startMxTakaTakWorker(data.build())
                    }
                    clipText.contains("twitter.com") -> {
                        val data =
                            Data.Builder().putString(WORK_URL, extractLinks(clipText))
                        startTwitterWorker(data.build())
                    }
                    clipText.contains("likee") -> {
                        val data =
                            Data.Builder().putString(WORK_URL, extractLinks(clipText))
                                .putString(WORK_PLATFORM_NAME, "likee")
                        startShareChatWorker(data.build(), Utils.ROOT_DIRECTORY_LIKEE)
                    }
                    clipText.contains("mojapp") -> {
                        val data =
                            Data.Builder().putString(WORK_URL, extractLinks(clipText))
                        startMxTakaTakWorker(data.build())
                    }
                    clipText.contains("tiktok") -> {
                        val data =
                            Data.Builder().putString(WORK_URL, extractLinks(clipText))
                        startMxTakaTakWorker(data.build())
                    }
                }
            }
            previousCopiedText = clipText
        }
    }

    private fun startTwitterWorker(data: Data) {

        Log.e("Worker", "Twitter worker start called")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val networkRequest = OneTimeWorkRequest.Builder(TwitterWorker::class.java)
            .setInputData(data)
            .addTag(WORKER_TAG_TWITTER)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .beginUniqueWork(
                WORKER_TAG,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                networkRequest
            )
            .enqueue()

        WorkManager.getInstance(this).getWorkInfoByIdLiveData((networkRequest.id))
            .observe(this) { workInfo ->
                if (workInfo != null) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val downloadUrl = workInfo.outputData.getString(WORKER_KEY_DOWNLOAD_URL)
                        Utils.startDownload(
                            downloadUrl!!,
                            Utils.ROOT_DIRECTORY_TWITTER,
                            this,
                            if (downloadUrl.contains(".mp4")) getTwitterFileName(
                                "video"
                            ) else getTwitterFileName(
                                "image"
                            )
                        )
                    }
                }
            }
    }

    private fun getTwitterFileName(type: String): String {
        return if (type == "image") {
            System.currentTimeMillis().toString() + ".jpg"
        } else {
            System.currentTimeMillis().toString() + ".mp4"
        }
    }

    private fun startMxTakaTakWorker(data: Data) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val networkRequest = OneTimeWorkRequest.Builder(MxTakaTakWorker::class.java)
            .setInputData(data)
            .addTag(WORKER_TAG_TAKA_TAK)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .beginUniqueWork(
                WORKER_TAG,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                networkRequest
            )
            .enqueue()

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(networkRequest.id)
            .observe(this) { workInfo ->
                if (workInfo != null) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        Utils.startDownload(
                            workInfo.outputData.getString(WORKER_KEY_DOWNLOAD_URL)!!,
                            Utils.ROOT_DIRECTORY_MX_TAKA_TAK,
                            this,
                            getFileName(workInfo.outputData.getString(WORKER_KEY_DOWNLOAD_URL)!!)
                        )
                    }
                }
            }
    }

    private fun getInstagramMediaFileName(aString: String): String {
        return if (aString.contains(".mp4")) {
            "Insta" + System.currentTimeMillis().toString() + ".mp4"
        } else {
            "Insta" + System.currentTimeMillis().toString() + ".png"
        }
    }

    private fun getFileName(url: String?): String {
        return try {
            File(URL(url).path).name + ".mp4"
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            System.currentTimeMillis().toString() + ".mp4"
        }
    }

    private fun startShareChatWorker(data: Data, directory: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val networkRequest = OneTimeWorkRequest.Builder(ShareChatMediaWorker::class.java)
            .setInputData(data)
            .addTag(WORKER_TAG_SHARECHAT)
            .setConstraints(constraints)
            .build()

        Log.e("Worker", "Sharechat worker network request requested")

        WorkManager.getInstance(this)
            .beginUniqueWork(
                WORKER_TAG,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                networkRequest
            )
            .enqueue()

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(networkRequest.id)
            .observe(this) { workInfo ->
                if (workInfo != null) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val downloadUrl = workInfo.outputData.getString(WORKER_KEY_DOWNLOAD_URL)!!
                        Utils.startDownload(downloadUrl, directory, this, getFileName(downloadUrl))
                    }
                }
            }
    }

    private fun startJoshWorker(data: Data) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val networkRequest = OneTimeWorkRequest.Builder(JoshWorker::class.java)
            .setInputData(data)
            .addTag(WORKER_TAG)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .beginUniqueWork(
                WORKER_TAG,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                networkRequest
            )
            .enqueue()

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(networkRequest.id)
            .observe(this) { info ->
                if (info != null) {
                    if (info.state == WorkInfo.State.SUCCEEDED) {
                        PlaylistDownloader(
                            info.outputData.getString(WORKER_KEY_DOWNLOAD_URL)!!,
                            this@ClipTextObserverService
                        ).download()
                    }
                }
            }
    }

    private fun startWorker(data: Data) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val networkRequest = OneTimeWorkRequest.Builder(InstagramMediaWorker::class.java)
            .setInputData(data)
            .addTag(WORKER_TAG)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .beginUniqueWork(
                WORKER_TAG,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                networkRequest
            )
            .enqueue()

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(networkRequest.id)
            .observe(this) { workInfo ->
                if (workInfo != null) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {

                        val downloadUrls = workInfo.outputData.getStringArray(
                            WORKER_KEY_DOWNLOAD_URL
                        )!!.toSet().toList()

                        for (downloadUrl in downloadUrls) {
                            Utils.startDownload(
                                downloadUrl,
                                Utils.ROOT_DIRECTORY_INSTAGRAM,
                                this,
                                getInstagramMediaFileName(downloadUrl)
                            )
                        }
                    } else if (workInfo.state == WorkInfo.State.FAILED) {
                        Utils.createToast(this, "Couldn't download file")
                    }
                }
            }
    }

    private fun extractLinks(str: String): String {
        val matcher: Matcher = Patterns.WEB_URL.matcher(str)
        if (!matcher.find()) {
            return ""
        }
        return matcher.group()
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
            Utils.createToast(applicationContext, "Enter valid Url")
            ""
        }
    }
}