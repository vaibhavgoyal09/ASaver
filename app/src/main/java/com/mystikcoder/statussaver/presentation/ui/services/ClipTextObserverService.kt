package com.mystikcoder.statussaver.presentation.ui.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Patterns
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.extensions.getClipboardText
import com.mystikcoder.statussaver.extensions.getFileName
import com.mystikcoder.statussaver.extensions.showShortToast
import com.mystikcoder.statussaver.presentation.framework.videoconverter.PlaylistDownloader
import com.mystikcoder.statussaver.presentation.ui.activity.HomeActivity
import com.mystikcoder.statussaver.presentation.ui.workers.*
import com.mystikcoder.statussaver.presentation.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.net.URI
import java.util.regex.Matcher
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("InlinedApi")
class ClipTextObserverService : LifecycleService(), ClipboardManager.OnPrimaryClipChangedListener {

    @Inject
    lateinit var preferences: Preferences

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var workManager: WorkManager

    private lateinit var clipboard: ClipboardManager
    private lateinit var notificationManager: NotificationManager
    private val isObserving: MutableLiveData<Boolean> = MutableLiveData()
    private var _isObserving: Boolean = false
    private lateinit var previousCopiedText: String

    companion object {
        val isServiceKilled: MutableLiveData<Boolean> = MutableLiveData()
        const val REQUEST_STORAGE_INTENT_RC = 444444
        const val REQUEST_STORAGE_NOTIFICATION_ID = 46461
    }

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
                }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                this,
                646,
                Intent(this, ClipTextObserverService::class.java).also {
                    it.action = RESUME_SERVICE
                }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
                    }, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
            if (NetworkState.isNetworkAvailable()) {
                if (Build.VERSION.SDK_INT >= 29) {
                    setupListeners()
                } else {
                    if (Utils.hasWritePermission(this)) {
                        setupListeners()
                    } else {
                        showStorageRequireNotification()
                    }
                }
            } else {
                showShortToast("No Internet connection available")
            }
        }
    }

    private fun showStorageRequireNotification() {
        val intent = Intent(this, HomeActivity::class.java).also {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_STORAGE_INTENT_RC,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel(
                this.resources.getString(R.string.app_name),
                "No Permission",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                notificationManager.createNotificationChannel(this)
            }

            val notificationBuilder =
                NotificationCompat.Builder(this, this.resources.getString(R.string.app_name))
                    .setSmallIcon(R.drawable.ic_status_splash)
                    .setAutoCancel(true)
                    .setColor(ContextCompat.getColor(this, R.color.blue_grey_500))
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            this.resources,
                            R.drawable.ic_status_splash
                        )
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(-1)
                    .setContentTitle("Require Storage Permission")
                    .setContentText("Asaver needs storage permission to download files")
                    .setFullScreenIntent(pendingIntent, true)

            notificationManager.notify(REQUEST_STORAGE_NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun setupListeners() {
        val clipText = clipboard.getClipboardText(this)

        if (clipText == previousCopiedText || clipText.isNullOrEmpty()) {
            return
        }

        when {

            clipText.contains(INSTAGRAM) -> {
                val urlWithoutQP =
                    "${getUrlWithoutParameters(extractLinks(clipText))}$INSTAGRAM_PARAMATERS"

                val data = Data.Builder()
                    .putString(WORK_URL, urlWithoutQP)
                    .putString(
                        WORK_COOKIES,
                        "ds_user_id=${preferences.getString(USER_ID)}; sessionid=${
                            preferences.getString(
                                SESSION_ID
                            )
                        }"
                    )
                    .build()
                startInstagramWorker(data)

            }

            clipText.contains(SHARE_CHAT) -> {
                val data =
                    Data.Builder().putString(WORK_URL, extractLinks(clipText))
                        .build()

                startShareChatWorker(data)
            }

            clipText.contains(ROPOSO) -> {
                val data = Data.Builder().putString(WORK_URL, clipText)
                    .build()

                startRoposoWorker(data)
            }
//            clipText.contains(FACEBOOK) || clipText.contains("fb") -> {
//                val data = Data.Builder().putString(WORK_URL, clipText)
//                    .build()
//
//                startShareChatWorker(data, Utils.ROOT_DIRECTORY_FACEBOOK)
//            }

            clipText.contains(MITRON) -> {

                val split = extractLinks(clipText).split("=")

                val data = Data.Builder().putString(
                    WORK_URL,
                    arrayOf("https://web.mitron.tv/video/" + split[split.size - 1])[0]
                ).build()

                startMitronWorker(data)
            }

            clipText.contains(CHINGARI) -> {

                val data =
                    Data.Builder().putString(WORK_URL, extractLinks(clipText))
                        .build()

                startChingariWorker(data)
            }

            clipText.contains(JOSH) -> {
                val data =
                    Data.Builder().putString(WORK_URL, extractLinks(clipText))
                        .build()

                startJoshWorker(data)
            }

            clipText.contains(MX_TAKA_TAK) -> {
                val data =
                    Data.Builder().putString(WORK_URL, extractLinks(clipText))
                startMxTakaTakWorker(data.build(), Utils.ROOT_DIRECTORY_MX_TAKA_TAK)
            }

            clipText.contains(TWITTER) -> {
                val data =
                    Data.Builder().putString(WORK_URL, extractLinks(clipText))
                startTwitterWorker(data.build())
            }

            clipText.contains(LIKEE) -> {
                val data =
                    Data.Builder().putString(WORK_URL, extractLinks(clipText))
                        .putString(WORK_PLATFORM_NAME, LIKEE)
                startLikeeWorker(data.build())
            }

            clipText.contains(MOJ) -> {
                val data =
                    Data.Builder().putString(WORK_URL, extractLinks(clipText))
                startMxTakaTakWorker(data.build(), Utils.ROOT_DIRECTORY_MOJ)
            }

            clipText.contains(TIKTOK) -> {
                val data =
                    Data.Builder().putString(WORK_URL, extractLinks(clipText))
                startMxTakaTakWorker(data.build(), Utils.ROOT_DIRECTORY_TIK_TOK)
            }

            else -> Unit
        }

        previousCopiedText = clipText
    }

    private fun startMitronWorker(data: Data) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val networkRequest = OneTimeWorkRequest.Builder(MitronWorker::class.java)
            .setInputData(data)
            .addTag(WORKER_TAG_MITRON)
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
                        val downloadUrl = workInfo.outputData.getString(WORKER_KEY_DOWNLOAD_URL)!!
                        Utils.startDownload(
                            downloadUrl,
                            Utils.ROOT_DIRECTORY_MITRON,
                            this,
                            downloadUrl.getFileName(
                                MITRON
                            )
                        )
                    }
                }
            }
    }

    private fun startChingariWorker(data: Data) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val networkRequest = OneTimeWorkRequest.Builder(ChingariWorker::class.java)
            .setInputData(data)
            .addTag(WORKER_TAG_CHINGARI)
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
                        val downloadUrl = workInfo.outputData.getString(WORKER_KEY_DOWNLOAD_URL)!!
                        Utils.startDownload(
                            downloadUrl,
                            Utils.ROOT_DIRECTORY_CHINGARI,
                            this,
                            downloadUrl.getFileName(
                                CHINGARI
                            )
                        )
                    }
                }
            }

    }

    private fun startRoposoWorker(data: Data) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val networkRequest = OneTimeWorkRequest.Builder(RoposoWorker::class.java)
            .setInputData(data)
            .addTag(WORKER_TAG_ROPOSO)
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
                        val downloadUrl = workInfo.outputData.getString(WORKER_KEY_DOWNLOAD_URL)!!
                        Utils.startDownload(
                            downloadUrl,
                            Utils.ROOT_DIRECTORY_ROPOSSO,
                            this,
                            downloadUrl.getFileName(
                                ROPOSO
                            )
                        )
                    }
                }
            }
    }

    private fun startLikeeWorker(data: Data) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val networkRequest = OneTimeWorkRequest.Builder(LikeeWorker::class.java)
            .setInputData(data)
            .addTag(WORKER_TAG_LIKEE)
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
                        val downloadUrl = workInfo.outputData.getString(WORKER_KEY_DOWNLOAD_URL)!!
                        Utils.startDownload(
                            downloadUrl,
                            Utils.ROOT_DIRECTORY_LIKEE,
                            this,
                            downloadUrl.getFileName(
                                LIKEE
                            )
                        )
                    }
                }
            }
    }

    private fun startTwitterWorker(data: Data) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val networkRequest = OneTimeWorkRequest.Builder(TwitterWorker::class.java)
            .setInputData(data)
            .addTag(WORKER_TAG_TWITTER)
            .setConstraints(constraints)
            .build()

        workManager.beginUniqueWork(
            WORKER_TAG,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            networkRequest
        )
            .enqueue()

        workManager.getWorkInfoByIdLiveData((networkRequest.id))
            .observe(this) { workInfo ->
                if (workInfo != null) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val downloadUrl = workInfo.outputData.getString(WORKER_KEY_DOWNLOAD_URL)
                        Utils.startDownload(
                            downloadUrl!!,
                            Utils.ROOT_DIRECTORY_TWITTER,
                            this,
                            downloadUrl.getFileName(TWITTER)
                        )
                    }
                }
            }
    }

    private fun startMxTakaTakWorker(data: Data, directory: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val networkRequest = OneTimeWorkRequest.Builder(MxTakaTakWorker::class.java)
            .setInputData(data)
            .addTag(WORKER_TAG_TAKA_TAK)
            .setConstraints(constraints)
            .build()

        workManager.beginUniqueWork(
            WORKER_TAG,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            networkRequest
        )
            .enqueue()

        workManager.getWorkInfoByIdLiveData(networkRequest.id)
            .observe(this) { workInfo ->
                if (workInfo != null) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val downloadUrl = workInfo.outputData.getString(WORKER_KEY_DOWNLOAD_URL)!!
                        Utils.startDownload(
                            downloadUrl,
                            directory,
                            this,
                            downloadUrl.getFileName(MX_TAKA_TAK)
                        )
                    }
                }
            }
    }

    private fun startShareChatWorker(data: Data) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val networkRequest = OneTimeWorkRequest.Builder(ShareChatMediaWorker::class.java)
            .setInputData(data)
            .addTag(WORKER_TAG_SHARECHAT)
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
                        val downloadUrl = workInfo.outputData.getString(WORKER_KEY_DOWNLOAD_URL)!!
                        Utils.startDownload(
                            downloadUrl,
                            Utils.ROOT_DIRECTORY_SHARECHAT,
                            this,
                            downloadUrl.getFileName(
                                SHARE_CHAT
                            )
                        )
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

                        val downloadUrl = info.outputData.getString(WORKER_KEY_DOWNLOAD_URL)!!

                        PlaylistDownloader(
                            downloadUrl,
                            downloadUrl.getFileName(JOSH),
                            this@ClipTextObserverService
                        ).download()
                    }
                }
            }
    }

    private fun startInstagramWorker(data: Data) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val networkRequest = OneTimeWorkRequest.Builder(InstagramWorker::class.java)
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
                                downloadUrl.getFileName(INSTAGRAM)
                            )
                        }
                    } else if (workInfo.state == WorkInfo.State.FAILED) {
                        showShortToast("Couldn't download file")
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
            showShortToast("Enter valid Url")
            ""
        }
    }
}