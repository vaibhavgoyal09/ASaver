package com.mystikcoder.statussaver.framework.extensions

import android.app.Activity
import android.widget.Toast
import com.mystikcoder.statussaver.framework.utils.Utils.extractLinks

fun Activity.getIntentExtras(): String? {

    if (intent.extras != null) {
        for (str in intent.extras?.keySet()!!) {
            if (str == "android.intent.extra.TEXT") {
                return extractLinks(intent.extras?.getString(str)!!)
            }
        }
    }
    return null
}

fun Activity.startAnotherApp(primaryPackage: String, secondaryPackage: String? = null) {
    try {
        val intent1 = packageManager.getLaunchIntentForPackage(primaryPackage)
        intent1?.let {
            startActivity(it)
        } ?: kotlin.run {
            if (secondaryPackage != null) {
                val intent2 = packageManager.getLaunchIntentForPackage(secondaryPackage)
                startActivity(intent2)
            } else {
                Toast.makeText(this, "App not Available", Toast.LENGTH_SHORT).show()
            }
        }
    }catch (e: Exception) {
        Toast.makeText(this, "App not Available", Toast.LENGTH_SHORT).show()
    }
}