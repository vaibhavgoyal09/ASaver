package com.mystikcoder.statussaver.presentation.utils

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Patterns
import java.io.File
import java.util.regex.Matcher

@Suppress("Deprecation")
object Utils {

    const val DIRECTORY_FACEBOOK = "/Asaver/Facebook/"
    const val DIRECTORY_INSTAGRAM = "/Asaver/Instagram/"
    const val DIRECTORY_LIKEE = "/Asaver/Likee/"
    const val DIRECTORY_ROPOSSO = "/Asaver/Roposso/"
    const val DIRECTORY_SHARECHAT = "/Asaver/ShareChat/"
    const val DIRECTORY_TWITTER = "/Asaver/Twitter/"
    const val DIRECTORY_MOJ = "/Asaver/Moj/"
    const val DIRECTORY_CHINGARI = "/Asaver/Chingari/"
    const val DIRECTORY_MX_TAKA_TAK = "/Asaver/MxTakaTak/"
    const val DIRECTORY_MITRON = "/Asaver/Mitron/"
    const val DIRECTORY_JOSH = "/Asaver/Josh/"
    const val DIRECTORY_TIK_TOK = "/Asaver/TikTok"

    const val MX_TAKA_TAK_URL = "http://androidqueue.com/tiktokapi/api.php"

    val DIRECTORY_ASAVER =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver")

    val DIRECTORY_INSTAGRAM_FILE =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + DIRECTORY_INSTAGRAM)

    val DIRECTORY_TWITTER_FILE =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + DIRECTORY_TWITTER)

    val DIRECTORY_TIK_TOK_FILE =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + DIRECTORY_TIK_TOK)

    val DIRECTORY_MOJ_FILE =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + DIRECTORY_MOJ)

    val DIRECTORY_MX_TAKA_TAK_FILE =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + DIRECTORY_MX_TAKA_TAK)

    val DIRECTORY_CHINGARI_FILE =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + DIRECTORY_CHINGARI)

    val DIRECTORY_MITRON_FILE =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + DIRECTORY_MITRON)

    val DIRECTORY_JOSH_FILE =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + DIRECTORY_JOSH)

    val DIRECTORY_LIKEE_FILE =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + DIRECTORY_LIKEE)

    val DIRECTORY_ROPOSSO_FILE =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + DIRECTORY_ROPOSSO)

    val DIRECTORY_SHARECHAT_FILE =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + DIRECTORY_SHARECHAT)

    val DIRECTORY_WHATSAPP_FILE =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/ASaver/WhatsApp")

    fun createFileFolder() {
        if (!DIRECTORY_INSTAGRAM_FILE.exists()) {
            DIRECTORY_INSTAGRAM_FILE.mkdirs()
        }
        if (!DIRECTORY_CHINGARI_FILE.exists()) {
            DIRECTORY_CHINGARI_FILE.mkdirs()
        }
        if (!DIRECTORY_LIKEE_FILE.exists()) {
            DIRECTORY_LIKEE_FILE.mkdirs()
        }
        if (!DIRECTORY_MITRON_FILE.exists()) {
            DIRECTORY_MITRON_FILE.mkdirs()
        }
        if (!DIRECTORY_TWITTER_FILE.exists()) {
            DIRECTORY_TWITTER_FILE.mkdirs()
        }
        if (!DIRECTORY_TIK_TOK_FILE.exists()) {
            DIRECTORY_TIK_TOK_FILE.mkdirs()
        }
        if (!DIRECTORY_MOJ_FILE.exists()) {
            DIRECTORY_MOJ_FILE.mkdirs()
        }
        if (!DIRECTORY_MX_TAKA_TAK_FILE.exists()) {
            DIRECTORY_MX_TAKA_TAK_FILE.mkdirs()
        }
        if (!DIRECTORY_JOSH_FILE.exists()) {
            DIRECTORY_JOSH_FILE.mkdirs()
        }
        if (!DIRECTORY_ROPOSSO_FILE.exists()) {
            DIRECTORY_ROPOSSO_FILE.mkdirs()
        }
        if (!DIRECTORY_SHARECHAT_FILE.exists()) {
            DIRECTORY_SHARECHAT_FILE.mkdirs()
        }
    }

    fun hasWritePermission(context: Context): Boolean {
        return context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun hasReadPermission(context: Context): Boolean {
        return context.checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun isNullOrEmpty(s: String?): Boolean {
        return s == null || s.isEmpty() || s.equals("null", ignoreCase = true) || s.equals(
            "0",
            ignoreCase = true
        )
    }

    fun startDownload(
        downloadPath: String,
        destinationPath: String,
        context: Context,
        fileName: String
    ) {
        val uri = Uri.parse(downloadPath)
        DownloadManager.Request(uri).also {
            it.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            it.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            it.setTitle(fileName + "")
            it.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DCIM,
                "$destinationPath$fileName"
            )
        }.apply {
            (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(this)
        }
    }

    fun extractLinks(str: String): String {
        val matcher: Matcher = Patterns.WEB_URL.matcher(str)
        if (!matcher.find()) {
            return ""
        }
        return matcher.group()
    }
}
