package com.mystikcoder.statussaver.extensions

import com.mystikcoder.statussaver.presentation.utils.*

fun String.getFileName(appName: String): String {

    return if (this.contains(".mp4")) {

        when (appName) {
            CHINGARI -> "Chingari${this.getRandom()}.mp4"
            FACEBOOK -> "Facebook${this.getRandom()}.mp4"
            TWITTER -> "Twitter${this.getRandom()}.mp4"
            INSTAGRAM -> "Instagram${this.getRandom()}.mp4"
            MOJ -> "Moj${this.getRandom()}.mp4"
            JOSH -> "Josh${this.getRandom()}.mp4"
            MITRON -> "Mitron${this.getRandom()}.mp4"
            TIKTOK -> "TikTok${this.getRandom()}.mp4"
            SHARE_CHAT -> "ShareChat${this.getRandom()}.mp4"
            MX_TAKA_TAK -> "MxTakaTak${this.getRandom()}.mp4"
            LIKEE -> "Roposo${this.getRandom()}.mp4"
            ROPOSO -> "Likee${this.getRandom()}.mp4"
            else -> "Asaver${this.getRandom()}.mp4"
        }

    } else {

        when (appName) {
            CHINGARI -> "Chingari${this.getRandom()}.png"
            FACEBOOK -> "Facebook${this.getRandom()}.png"
            TWITTER -> "Twitter${this.getRandom()}.png"
            INSTAGRAM -> "Instagram${this.getRandom()}.png"
            MOJ -> "Moj${this.getRandom()}.png"
            JOSH -> "Josh${this.getRandom()}.png"
            MITRON -> "Mitron${this.getRandom()}.png"
            TIKTOK -> "TikTok${this.getRandom()}.png"
            SHARE_CHAT -> "ShareChat${this.getRandom()}.png"
            MX_TAKA_TAK -> "MxTakaTak${this.getRandom()}.png"
            LIKEE -> "Roposo${this.getRandom()}.png"
            ROPOSO -> "Likee${this.getRandom()}.png"
            else -> "Asaver${this.getRandom()}.png"
        }
    }
}

fun String.getRandom(count: Int = 8): String {
    val alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz"
    val sb = StringBuilder(count)
    var i = 0
    while (i < count) {

        // generate a random number between
        // 0 to AlphaNumericString variable length
        val index = (alphaNumericString.length * Math.random()).toInt()

        // add Character one by one in end of sb
        sb.append(alphaNumericString.toCharArray()[index])
        i++
    }
    return sb.toString()
}