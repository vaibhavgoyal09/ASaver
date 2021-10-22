package com.mystikcoder.statussaver.framework.services

import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkManager
import com.mystikcoder.statussaver.framework.utils.Preferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationDownloadService : LifecycleService() {

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var preferences: Preferences

    private lateinit var notificationManager: NotificationManager

    private lateinit var currNotificationBuilder: NotificationCompat.Builder

    companion object {
        val isServiceKilled: MutableLiveData<Boolean> = MutableLiveData()

        const val NOTIFICATION_ID = 4523261
        const val KEY_TEXT_DOWNLOAD = "key_text_start_download"
    }


}