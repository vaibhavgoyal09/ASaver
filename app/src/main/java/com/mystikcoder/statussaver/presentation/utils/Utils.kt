package com.mystikcoder.statussaver.presentation.utils

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Patterns
import android.view.Gravity
import android.widget.Toast
import java.io.File
import java.util.regex.Matcher

@Suppress("DEPRECATION")
object Utils {

    const val ROOT_DIRECTORY_FACEBOOK = "/Asaver/Facebook/"
    const val ROOT_DIRECTORY_INSTAGRAM = "/Asaver/Instagram/"
    const val ROOT_DIRECTORY_LIKEE = "/Asaver/Likee/"
    const val ROOT_DIRECTORY_ROPOSSO = "/Asaver/Roposso/"
    const val ROOT_DIRECTORY_SHARECHAT = "/Asaver/ShareChat/"
    const val ROOT_DIRECTORY_TWITTER = "/Asaver/Twitter/"
    const val ROOT_DIRECTORY_MOJ = "/Asaver/Moj/"
    const val ROOT_DIRECTORY_CHINGARI = "/Asaver/Chingari/"
    const val ROOT_DIRECTORY_MX_TAKA_TAK = "/Asaver/MxTakaTak/"
    const val ROOT_DIRECTORY_MITRON = "/Asaver/Mitron/"
    const val ROOT_DIRECTORY_JOSH = "/Asaver/Josh/"
    const val ROOT_DIRECTORY_TIK_TOK = "/Asaver/TikTok"
    const val MX_TAKA_TAK_URL = "http://androidqueue.com/tiktokapi/api.php"

    val PATH_ROOT_DIRECTORY_APP =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver"

    val PATH_ROOT_DIRECTORY_FACEBOOK: File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver/Facebook")

    val PATH_ROOT_DIRECTORY_INSTAGRAM: File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver/Instagram")

    val PATH_ROOT_DIRECTORY_TWITTER: File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver/Twitter")

    val PATH_ROOT_DIRECTORY_LIKEE: File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver/Likee")

    val PATH_ROOT_DIRECTORY_WHATSAPP: File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver/WhatsApp")

    val PATH_ROOT_DIRECTORY_JOSH: File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver/Josh")

    val PATH_ROOT_DIRECTORY_MITRON: File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver/Mitron")

    val PATH_ROOT_DIRECTORY_MOJ: File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver/Moj")

    val PATH_ROOT_DIRECTORY_ROPOSSO: File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver/Roposo")

    val PATH_ROOT_DIRECTORY_CHINGARI: File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver/Chingari")

    val PATH_ROOT_DIRECTORY_SHARE_CHAT: File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver/ShareChat")

    val PATH_ROOT_DIRECTORY_MX_TAKA_TAK: File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver/MxTakaTak")

    val PATH_ROOT_DIRECTORY_TIK_TOK: File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Asaver/TikTok")

    fun createFileFolder() {

        if (!PATH_ROOT_DIRECTORY_FACEBOOK.exists()) {
            PATH_ROOT_DIRECTORY_FACEBOOK.mkdirs()
        }
        if (!PATH_ROOT_DIRECTORY_INSTAGRAM.exists()) {
            PATH_ROOT_DIRECTORY_INSTAGRAM.mkdirs()
        }
        if (!PATH_ROOT_DIRECTORY_LIKEE.exists()) {
            PATH_ROOT_DIRECTORY_LIKEE.mkdirs()
        }
        if (!PATH_ROOT_DIRECTORY_ROPOSSO.exists()) {
            PATH_ROOT_DIRECTORY_ROPOSSO.mkdirs()
        }
        if (!PATH_ROOT_DIRECTORY_SHARE_CHAT.exists()) {
            PATH_ROOT_DIRECTORY_SHARE_CHAT.mkdirs()
        }
        if (!PATH_ROOT_DIRECTORY_WHATSAPP.exists()) {
            PATH_ROOT_DIRECTORY_WHATSAPP.mkdirs()
        }
        if (!PATH_ROOT_DIRECTORY_TWITTER.exists()) {
            PATH_ROOT_DIRECTORY_TWITTER.mkdirs()
        }
        if (!PATH_ROOT_DIRECTORY_CHINGARI.exists()) {
            PATH_ROOT_DIRECTORY_CHINGARI.mkdirs()
        }
        if (!PATH_ROOT_DIRECTORY_MOJ.exists()) {
            PATH_ROOT_DIRECTORY_MOJ.mkdirs()
        }
        if (!PATH_ROOT_DIRECTORY_MX_TAKA_TAK.exists()) {
            PATH_ROOT_DIRECTORY_MX_TAKA_TAK.mkdirs()
        }
        if (!PATH_ROOT_DIRECTORY_JOSH.exists()) {
            PATH_ROOT_DIRECTORY_JOSH.mkdirs()
        }
        if (!PATH_ROOT_DIRECTORY_MITRON.exists()) {
            PATH_ROOT_DIRECTORY_MITRON.mkdirs()
        }
        if (!PATH_ROOT_DIRECTORY_TIK_TOK.exists()) {
            PATH_ROOT_DIRECTORY_TIK_TOK.mkdirs()
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
        FileName: String
    ) {
        val uri = Uri.parse(downloadPath)
        DownloadManager.Request(uri).also {
            it.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            it.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            it.setTitle(FileName + "")
            it.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "$destinationPath$FileName"
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
