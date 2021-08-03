package com.mystikcoder.statussaver.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mystikcoder.statussaver.R

object DialogUtil {

    @SuppressLint("StaticFieldLeak")
    private var dialog: BottomSheetDialog? = null

    fun openBottomSheetDialog(context: Activity) {
        dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)

        val view = LayoutInflater.from(context).inflate(
            R.layout.download_tutorial_automatic,
            context.findViewById<LinearLayout>(R.id.linearContainer)
        )

        if (Build.VERSION.SDK_INT >= 29) {
            view.findViewById<LinearLayout>(R.id.layoutAutomaticDownload).visibility = View.GONE
        }

        dialog?.setContentView(view)
        dialog?.show()
    }

    fun hideSheet(){
        dialog?.cancel()
        dialog = null
    }

    fun isSheetShowing(): Boolean{
        return dialog?.isShowing ?: false
    }
}