package com.mystikcoder.statussaver.presentation.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.ActivityLoginBinding
import com.mystikcoder.statussaver.presentation.utils.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("SetJavaScriptEnabled")
class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var preferences: Preferences

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        loadPage()
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadPage()
        }
    }

    private fun loadPage() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.clearCache(true)
        binding.webView.webViewClient = MyBrowser()

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(binding.webView, true)
            flush()
            removeAllCookies(null)
            removeSessionCookies(null)
        }
        binding.webView.loadUrl("https://www.instagram.com/accounts/login/")
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                binding.swipeRefreshLayout.isRefreshing = progress != 100
            }
        }
    }

    fun getCookie(siteName: String, CookieName: String): String? {

        /* -------------------- WARNING ---------------------------*/

//       (Do not change anything otherwise app will not be able to retrieve cookies)

        val cookies = CookieManager.getInstance().getCookie(siteName)
        if (!cookies.isNullOrEmpty()) {
            for (aString in cookies.split("; ")) {
                if (aString.contains(CookieName)) {
                    return aString.split("=".toRegex()).toTypedArray()[1]
                }
            }
        }
        return null
    }

    private inner class MyBrowser : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            view?.loadUrl(url!!)
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            val cookies = CookieManager.getInstance().getCookie(url)
            Log.e("TAG", "cookies   $cookies")

            try {
                url?.let {
                    val sessionId: String? = getCookie(it, "sessionid")
                    val csrfToken: String? = getCookie(it, "csrftoken")
                    val userid: String? = getCookie(it, "ds_user_id")

                    Log.e(
                        "TAG",
                        "sessionId   $sessionId  csrfToken   $csrfToken   userId   $userid"
                    )

                    if (sessionId != null && csrfToken != null && userid != null) {
                        preferences.putString(COOKIES, cookies)
                        preferences.putString(CSRF, csrfToken)
                        preferences.putString(SESSION_ID, sessionId)
                        preferences.putString(USER_ID, userid)
                        preferences.putBoolean(IS_INSTA_LOGGED_IN, true)
                        binding.webView.destroy()
                        val intent = Intent()
                        intent.putExtra("result", "result")
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
