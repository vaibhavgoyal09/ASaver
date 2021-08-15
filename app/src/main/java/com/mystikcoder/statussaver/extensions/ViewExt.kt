package com.mystikcoder.statussaver.extensions

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar

fun View.showRequestPermissionSnackbar(activity: Activity) {
    Snackbar.make(
        this,
        "App needs storage permission to download files",
        Snackbar.LENGTH_LONG
    ).setAction("Ok") {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            2
        )
    }.show()
}

fun View.showSettingsSnackbar(activity: Activity) {
    Snackbar.make(
        this,
        "App needs storage permission to download files",
        Snackbar.LENGTH_LONG
    ).setAction("Settings") {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
            it.data =
                Uri.fromParts(
                    "package",
                    "com.mystikcoder.statussaver",
                    null
                )
            activity.startActivity(it)
        }
    }.show()
}