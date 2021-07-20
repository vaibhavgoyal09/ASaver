package com.mystikcoder.statussaver.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PrefManager(val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)

    fun putString(key: String, value: String) {
        sharedPreferences.edit {
            this.putString(key, value)
            this.apply()
        }
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, "")
    }

    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit {
            this.putBoolean(key, value)
            this.apply()
        }
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun putInt(key: String, value: Int) {
        sharedPreferences.edit {
            this.putInt(key, value)
            this.apply()
        }
    }

    fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun clearFacebookPrefs() {
        putBoolean(IS_FB_LOGGED_IN, false)
        putString(FB_KEY, "")
        putString(FB_COOKIES, "")
    }

    fun clearInstagramPrefs() {
        putBoolean(IS_INSTA_LOGGED_IN, false)
        putString(COOKIES, "")
        putString(USER_ID, "")
        putString(CSRF, "")
        putString(SESSION_ID, "")
    }

    fun clearPrefs() {
        sharedPreferences.edit {
            this.clear()
            this.apply()
        }
    }
}