package com.mystikcoder.statussaver.utils

import android.content.Context
import android.content.Intent
import com.mystikcoder.statussaver.ui.activity.FullImageViewActivity
import com.mystikcoder.statussaver.ui.activity.VideoPlayActivity
import java.io.File

class FileClicked(private val context: Context) {

    fun onFileClicked(file: File) {
        val extension = file.name.substring(file.name.lastIndexOf("."))
        if (extension == ".mp4") {
            Intent(context, VideoPlayActivity::class.java).also {
                it.putExtra("videoUri", file.path)
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
        } else {
            Intent(context, FullImageViewActivity::class.java).also {
                it.putExtra("imageUri", file.path)
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
        }
    }
}
