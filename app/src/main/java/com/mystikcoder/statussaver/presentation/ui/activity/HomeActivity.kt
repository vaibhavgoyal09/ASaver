package com.mystikcoder.statussaver.presentation.ui.activity

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Patterns.WEB_URL
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.ActivityHomeScreenBinding
import com.mystikcoder.statussaver.extensions.*
import com.mystikcoder.statussaver.presentation.framework.events.common.DownloadRequestEvent
import com.mystikcoder.statussaver.presentation.ui.services.ClipTextObserverService
import com.mystikcoder.statussaver.presentation.ui.viewmodel.HomeViewModel
import com.mystikcoder.statussaver.presentation.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.util.regex.Matcher

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var clipboard: ClipboardManager
    private var isFirstTimeClicked: Boolean = true
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_StatusSaverUI)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_screen)

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Intent(applicationContext, ClipTextObserverService::class.java).also {
                it.action = START_SERVICE
                startService(it)
            }
        }
        initViews()
        checkPermission()
        setupStateObservers()
    }

    private fun setupStateObservers() {

        NetworkState.observe(this) {
            binding.buttonDownload.isClickable = it
        }

        lifecycleScope.launchWhenStarted {
            viewModel.downloadRequestState.collect { event ->
                when (event) {
                    is DownloadRequestEvent.Loading -> {
                        showProgressBar()
                    }
                    is DownloadRequestEvent.Success -> {
                        hideProgressBar()
                    }
                    is DownloadRequestEvent.Error -> {
                        hideProgressBar()
                        applicationContext.showShortToast(event.errorMessage)
                    }
                    else -> hideProgressBar()
                }
            }
        }
    }

    private fun initViews() {

        isFirstTimeClicked = true

        ClipTextObserverService.isServiceKilled.observe(this) { yes ->

            if (yes) {
                binding.layoutMisc.visibility = View.VISIBLE
                binding.textEnable.setOnClickListener {
                    Intent(this, ClipTextObserverService::class.java).also { intent ->
                        intent.action = START_SERVICE
                        startService(intent)
                    }
                }
            } else {
                binding.layoutMisc.visibility = View.GONE
            }
        }

        binding.buttonPasteLink.setOnClickListener {
            pasteLink()
        }

        binding.layoutShareApp.setOnClickListener {

            val link =
                "\n" + "https://play.google.com/store/apps/details?id=com.mystikcoder.statussaver"

            Intent(Intent.ACTION_SEND).also {
                it.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name))
                it.putExtra(
                    Intent.EXTRA_TEXT,
                    resources.getString(R.string.share_app_message) + link
                )
                it.type = "text/plain"
            }.apply {
                Intent.createChooser(this, "Share app using")
                    .also {
                        startActivity(it)
                    }
            }
        }

        binding.layoutRateApp.setOnClickListener {
            try {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.mystikcoder.statussaver")
                ).also {
                    startActivity(it)
                }
            } catch (e: Exception) {
                applicationContext.showShortToast("App not Found")
            }
        }

        binding.layoutPrivacyPolicy.setOnClickListener {
            Intent(
                Intent.ACTION_VIEW
            ).also {
                it.data = Uri.parse(PRIVACY_POLICY_URL)
                startActivity(it)
            }
        }

//        binding.layoutFacebook.setOnClickListener {
//            Intent(
//                applicationContext,
//                FacebookActivity::class.java
//            ).also {
//                startActivity(it)
//            }
//        }

        binding.layoutInstagram.setOnClickListener {
            Intent(
                applicationContext,
                InstagramActivity::class.java
            ).also {
                startActivity(it)
            }
        }

        binding.layoutTwitter.setOnClickListener {
            Intent(
                applicationContext,
                TwitterActivity::class.java
            ).also {
                startActivity(it)
            }
        }

        binding.layoutTikTok.setOnClickListener {
            Intent(
                applicationContext,
                TikTokActivity::class.java
            ).also {
                startActivity(it)
            }
        }

        binding.layoutLikee.setOnClickListener {
            Intent(
                applicationContext,
                LikeeActivity::class.java
            ).also {
                startActivity(it)
            }
        }

        binding.layoutDownloads.setOnClickListener {
            Intent(
                applicationContext,
                GalleryActivity::class.java
            ).also {
                startActivity(it)
            }
        }

        binding.layoutRoposso.setOnClickListener {
            Intent(
                applicationContext,
                RoposoActivity::class.java
            ).also {
                startActivity(it)
            }
        }

        binding.layoutShareChat.setOnClickListener {
            Intent(
                applicationContext,
                ShareChatActivity::class.java
            ).also {
                startActivity(it)
            }
        }

        binding.layoutJosh.setOnClickListener {
            Intent(
                applicationContext,
                JoshActivity::class.java
            ).also {
                startActivity(it)
            }
        }

        binding.layoutTakaTak.setOnClickListener {
            Intent(
                applicationContext,
                MxTakaTakActivity::class.java
            ).also {
                startActivity(it)
            }
        }

        binding.layoutMitron.setOnClickListener {
            Intent(
                applicationContext,
                MitronActivity::class.java
            ).also {
                startActivity(it)
            }
        }

        binding.layoutChingari.setOnClickListener {
            Intent(
                applicationContext,
                ChingariActivity::class.java
            ).also {
                startActivity(it)
            }
        }

        binding.layoutMoj.setOnClickListener {
            Intent(
                applicationContext,
                MojActivity::class.java
            ).also {
                startActivity(it)
            }
        }

        binding.buttonDownload.setOnClickListener {

            if (NetworkState.isNetworkAvailable()) {
                if (Build.VERSION.SDK_INT >= 29) {
                    setupListeners()
                } else {
                    if (Utils.hasWritePermission(applicationContext)) {
                        setupListeners()
                    } else {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        ) {
                            binding.root.showSettingsSnackbar(this)
                        } else {
                            binding.root.showRequestPermissionSnackbar(this)
                        }
                    }
                }
            } else {
                applicationContext.showShortToast("No Internet connection available")
            }
        }
    }

    private fun pasteLink() {
        binding.inputLink.setText(clipboard.getClipboardText(applicationContext))
    }

    override fun onResume() {
        super.onResume()

        getIntentExtras()?.let {
            binding.inputLink.setText(it)
        } ?: kotlin.run {
            clipboard.getClipboardText(applicationContext)?.let {
                binding.inputLink.setText(extractLinks(it))
            }
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 29) {
            if (!Utils.hasReadPermission(this)) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            }
        } else {
            if (!Utils.hasWritePermission(this)) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    2
                )
            } else {
                Utils.createFileFolder()
            }
        }
    }

    private fun setupListeners() {
        if (binding.inputLink.text.isEmpty()) {
            applicationContext.showShortToast("Enter Link")
        } else if (!WEB_URL.matcher(extractLinks(binding.inputLink.text.toString()))
                .matches()
        ) {
            showShortToast("Enter valid link")
        } else {
            when {
                binding.inputLink.text.contains("instagram") -> {
                    callInstagramDownload()
                }
//                binding.inputLink.text.contains("facebook") -> {
//                    callFacebookDownload()
//                }
//                binding.inputLink.text.contains("fb") -> {
//                    callFacebookDownload()
//                }
                binding.inputLink.text.contains("sharechat") -> {
                    callShareChatDownload()
                }
                binding.inputLink.text.contains("roposo") -> {
                    callRoposoDownload()
                }
                binding.inputLink.text.contains("mojapp") -> {
                    callMojDownload()
                }
                binding.inputLink.text.contains("mitron") -> {
                    callMitronDownload()
                }
                binding.inputLink.text.contains("josh") -> {
                    callJoshDownload()
                }
                binding.inputLink.text.contains("chingari") -> {
                    callChingariDownload()
                }
                binding.inputLink.text.contains("twitter") -> {
                    callTwitterDownload()
                }
                binding.inputLink.text.contains("takatak") -> {
                    callMxTakaTakDownload()
                }
                binding.inputLink.text.contains("likee") -> {
                    callLikeeDownload()
                }
                binding.inputLink.text.toString().contains("tiktok") -> {
                    callTikTokDownload()
                }
                else -> showShortToast("App not supported")
            }
        }
    }

    private fun extractLinks(str: String): String {
        val matcher: Matcher = WEB_URL.matcher(str)
        if (!matcher.find()) {
            return ""
        }
        return matcher.group()
    }

    private fun showProgressBar() {
        binding.buttonDownload.visibility = View.GONE
        binding.buttonPasteLink.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.inputLink.setText("")
        binding.buttonDownload.visibility = View.VISIBLE
        binding.buttonPasteLink.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun callInstagramDownload() {
        viewModel.downloadInstagram(extractLinks(binding.inputLink.text.toString()))
    }

//    private fun callFacebookDownload() {
//        viewModel.downloadFacebook(extractLinks(binding.inputLink.text.toString()))
//    }

    private fun callLikeeDownload() {
        viewModel.downloadLikee(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callRoposoDownload() {
        viewModel.downloadRoposo(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callMojDownload() {
        viewModel.downloadMoj(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callMitronDownload() {
        val split = (extractLinks(binding.inputLink.text.toString())).split("=")
        viewModel.downloadMitron(arrayOf("https://web.mitron.tv/video/" + split[split.size - 1])[0])
    }

    private fun callTwitterDownload() {
        viewModel.downloadTwitter(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callMxTakaTakDownload() {
        viewModel.downloadMxTakaTak(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callChingariDownload() {
        viewModel.downloadChingari(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callJoshDownload() {
        viewModel.downloadJosh(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callShareChatDownload() {
        viewModel.downloadShareChat(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callTikTokDownload() {
        viewModel.downloadTikTok(extractLinks(binding.inputLink.text.toString()))
    }

    override fun onBackPressed() {
        if (isFirstTimeClicked) {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
            isFirstTimeClicked = false
            Handler(mainLooper).postDelayed({ isFirstTimeClicked = true }, 3000L)
            return
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Intent(applicationContext, ClipTextObserverService::class.java).also {
            it.action = STOP_SERVICE
            startService(it)
        }
    }
}
