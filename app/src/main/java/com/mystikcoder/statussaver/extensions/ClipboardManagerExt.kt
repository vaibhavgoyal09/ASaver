package com.mystikcoder.statussaver.extensions

import android.content.ClipboardManager
import android.content.Context

fun ClipboardManager.getClipboardText(context: Context): String? {
    if (hasPrimaryClip()) {
        val clip = primaryClip
        if (clip != null && clip.itemCount > 0) {
            val clipboardText = clip.getItemAt(0).coerceToText(context)
            if (clipboardText != null)
                return clipboardText.toString()
        }
    }
    return null
}