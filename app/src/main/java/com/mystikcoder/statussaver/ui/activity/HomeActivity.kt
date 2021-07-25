package com.mystikcoder.statussaver.ui.activity

import android.Manifest
import android.app.*
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.util.Patterns.WEB_URL
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.ActivityHomeScreenBinding
import com.mystikcoder.statussaver.services.ClipTextObserverService
import com.mystikcoder.statussaver.states.*
import com.mystikcoder.statussaver.states.facebook.FacebookEvent
import com.mystikcoder.statussaver.states.instagram.InstagramEvent
import com.mystikcoder.statussaver.utils.*
import com.mystikcoder.statussaver.videoconverter.PlaylistDownloader
import com.mystikcoder.statussaver.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.net.URI
import java.net.URL
import java.util.regex.Matcher
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    @Inject
    lateinit var prefManager: PrefManager

    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var clipboard: ClipboardManager
    private var clipText = ""
    private var intentText = ""
    private var isFirstTimeClicked: Boolean = true
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_StatusSaverUI)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_screen)

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        Intent(applicationContext, ClipTextObserverService::class.java).also {
            it.action = START_SERVICE
            startService(it)
        }
        initViews()
        checkPermission()
    }

    private fun initViews() {

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
        isFirstTimeClicked = true

        val clipboardText = clipboard.primaryClip?.getItemAt(0)?.text.toString()

        if (intent.extras != null) {
            for (str in intent.extras?.keySet()!!) {
                if (str == "android.intent.extra.TEXT") {
                    intentText = extractLinks(intent.extras?.getString(str)!!)
                }
            }
        } else {
           if (clipboard.primaryClipDescription != null){
               if (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)!!) {
                   binding.inputLink.setText(clipboardText)
                   clipText = clipboardText
               }
           }
        }

        if (intentText == "") {
            binding.inputLink.setText(extractLinks(clipText))
        } else {
            binding.inputLink.setText(extractLinks(intentText))
        }

        Log.e("", binding.inputLink.text.toString())

        binding.buttonPasteLink.setOnClickListener {
            pasteLink()
        }

        binding.layoutShareApp.setOnClickListener {
            Utils.shareApp(applicationContext)
        }

        binding.layoutRateApp.setOnClickListener {
            Utils.rateApp(applicationContext)
        }

        binding.layoutFacebook.setOnClickListener {
            Intent(
                applicationContext,
                FacebookActivity::class.java
            ).also {
                startActivity(it)
            }
        }
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
            Intent(this, GalleryActivity::class.java).also {
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

            if (Utils.isNetworkAvailable(applicationContext)) {
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
                            Snackbar.make(
                                binding.root,
                                "App needs storage permission to download files",
                                Snackbar.LENGTH_LONG
                            ).setAction("Settings") {
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                                    it.data = Uri.fromParts(
                                        "package",
                                        "com.mystikcoder.statussaver",
                                        null
                                    )
                                    startActivity(it)
                                }
                            }.show()
                        } else {
                            Snackbar.make(
                                binding.root,
                                "App needs storage permission to download files",
                                Snackbar.LENGTH_LONG
                            ).setAction("Ok") {
                                ActivityCompat.requestPermissions(
                                    this,
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                    2
                                )
                            }.show()
                        }
                    }
                }
            } else {
                Utils.createToast(applicationContext, "No Internet connection available")
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.instagramData.collect { instagramDataEvent ->
                when (instagramDataEvent) {
                    is InstagramEvent.Loading -> {
                        showProgressBar()
                    }
                    is InstagramEvent.Success -> {
                        hideProgressBar()

                        instagramDataEvent.mediaUrl?.let {
                            Utils.createToast(applicationContext, "Download started")

                            binding.inputLink.setText("")
                            for (downloadUrl in instagramDataEvent.mediaUrl) {
                                Utils.startDownload(
                                    downloadUrl,
                                    Utils.ROOT_DIRECTORY_INSTAGRAM,
                                    applicationContext,
                                    getFileName(downloadUrl)
                                )
                            }
                        }
                    }
                    is InstagramEvent.Failure -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, instagramDataEvent.errorText)
                    }
                    else -> hideProgressBar()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.mitronData.collect { event ->
                when (event) {
                    is MitronEvent.Success -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, "Download Started")
                        Utils.startDownload(
                            event.videoUrl,
                            Utils.ROOT_DIRECTORY_MITRON,
                            applicationContext,
                            event.fileName
                        )
                    }
                    is MitronEvent.Failure -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, event.errorText)
                    }
                    else -> hideProgressBar()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.mxTakaTakData.collect { event ->
                when (event) {
                    is MxTakaTakEvent.Loading -> {
                        showProgressBar()
                    }
                    is MxTakaTakEvent.Success -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, "Download started")
                        Utils.startDownload(
                            event.mediaUrl,
                            Utils.ROOT_DIRECTORY_MX_TAKA_TAK,
                            applicationContext,
                            event.fileName
                        )
                    }
                    is MxTakaTakEvent.Failure -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, event.errorText)
                    }
                    else -> hideProgressBar()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.tiktikData.collect { event ->
                when (event) {
                    is TikTokEvent.Loading -> {
                        showProgressBar()
                    }
                    is TikTokEvent.Success -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, "Download started")
                        Utils.startDownload(
                            event.mediaUrl,
                            Utils.ROOT_DIRECTORY_TIK_TOK,
                            applicationContext,
                            event.fileName
                        )
                    }
                    is TikTokEvent.Failure -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, event.errorText)
                    }
                    else -> hideProgressBar()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.chingariData.collect { event ->
                when (event) {
                    is ChingariEvent.Success -> {
                        hideProgressBar()
                        binding.inputLink.setText("")
                        Utils.createToast(applicationContext, "Download Started")
                        Utils.startDownload(
                            event.videoUrl,
                            Utils.ROOT_DIRECTORY_CHINGARI,
                            applicationContext,
                            event.fileName
                        )
                    }
                    is ChingariEvent.Failure -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, event.errorText)
                    }
                    else -> hideProgressBar()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.facebookData.collect { event ->
                when (event) {
                    is FacebookEvent.Success -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, "Download started")
                        Utils.startDownload(
                            event.videoUrl,
                            Utils.ROOT_DIRECTORY_FACEBOOK,
                            applicationContext,
                            event.fileName
                        )
                    }
                    is FacebookEvent.Failure -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, event.errorText)
                    }
                    else -> hideProgressBar()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.joshData.collect { event ->
                when (event) {
                    is JoshEvent.Failure -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, event.errorText)
                    }
                    is JoshEvent.Success -> {
                        hideProgressBar()

                        kotlin.runCatching {
                            PlaylistDownloader(
                                event.videoUrl,
                                this@HomeActivity
                            ).download(
                                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath + "/Asaver/Josh/" + event.fileName
                            )
                        }.getOrElse {
                            Utils.createToast(
                                applicationContext,
                                it.message ?: "Couldn't download file"
                            )
                        }
                    }
                    else -> hideProgressBar()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.likeeData.collect { event ->
                when (event) {
                    is LikeeEvent.Failure -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, event.errorText)
                    }
                    is LikeeEvent.Success -> {
                        hideProgressBar()
                        Utils.startDownload(
                            event.videoUrl,
                            Utils.ROOT_DIRECTORY_LIKEE,
                            applicationContext,
                            event.fileName
                        )
                    }
                    else -> hideProgressBar()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.roposoData.collect { event ->
                when (event) {
                    is RopossoEvent.Success -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, "Download started")
                        Utils.startDownload(
                            event.videoUrl,
                            Utils.ROOT_DIRECTORY_ROPOSSO,
                            applicationContext,
                            event.fileName
                        )
                    }
                    is RopossoEvent.Failure -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, event.errorText)
                    }
                    else -> hideProgressBar()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.shareChatData.collect { event ->
                when (event) {
                    is ShareChatEvent.Success -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, "Download started")
                        Utils.startDownload(
                            event.videoUrl,
                            Utils.ROOT_DIRECTORY_SHARECHAT,
                            applicationContext,
                            event.fileName
                        )
                    }
                    is ShareChatEvent.Failure -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, event.errorText)
                    }
                    else -> hideProgressBar()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.twitterData.collect { event ->
                when (event) {
                    is TwitterEvent.Loading -> {
                        showProgressBar()
                    }
                    is TwitterEvent.Failure -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, event.errorText)
                    }
                    is TwitterEvent.Success -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, "Download Started")
                        Utils.startDownload(
                            event.mediaUrl,
                            Utils.ROOT_DIRECTORY_TWITTER,
                            applicationContext,
                            event.fileName
                        )
                    }
                    else -> hideProgressBar()
                }
            }
        }
    }

    private fun pasteLink() {

        if (intentText == "") {
            if (clipText.contains("instagram")
                || clipText.contains("twitter")
                || clipText.contains("josh")
                || clipText.contains("likee")
                || clipText.contains("share.mxtakatak.com")
                || clipText.contains("ropo")
                || clipText.contains("mojapp")
                || clipText.contains("sharechat")
                || clipText.contains("mitron")
                || clipText.contains("chingari")
                || clipText.contains("facebook")
            ) {
                binding.inputLink.setText(extractLinks(clipText))
            }
        } else {
            binding.inputLink.setText(extractLinks(intentText))
        }
    }

    override fun onResume() {
        super.onResume()
        if (intentText == "") {
            clipboard.primaryClip?.let {
                val clipboardText = it.getItemAt(0)?.text.toString()
                if (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)!!) {
                    binding.inputLink.setText(clipboardText)
                    clipText = clipboardText
                }
            }
        } else {
            clipText = intentText
        }
        pasteLink()
    }

    fun checkPermission() {
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
            Utils.createToast(applicationContext, "Enter Link")
        } else if (!WEB_URL.matcher(extractLinks(binding.inputLink.text.toString()))
                .matches()
        ) {
            Utils.createToast(applicationContext, "Enter valid link")
        } else {
            when {
                binding.inputLink.text.contains("instagram") -> {
                    Log.e("Download called: ", binding.inputLink.text.toString())
                    callInstagramDownload()
                }
                binding.inputLink.text.contains("facebook") -> {
                    Log.e("Download called: ", binding.inputLink.text.toString())
                    callFacebookDownload()
                }
                binding.inputLink.text.contains("sharechat") -> {
                    Log.e("Download called: ", binding.inputLink.text.toString())
                    callShareChatDownload()
                }
                binding.inputLink.text.contains("roposo") -> {
                    Log.e("Download called: ", binding.inputLink.text.toString())
                    callRoposoDownload()
                }
                binding.inputLink.text.contains("mojapp") -> {
                    Log.e("Download called: ", binding.inputLink.text.toString())
                    callMojDownload()
                }
                binding.inputLink.text.contains("mitron") -> {
                    Log.e("Download called: ", binding.inputLink.text.toString())
                    callMitronDownload()
                }
                binding.inputLink.text.contains("josh") -> {
                    Log.e("Download called: ", binding.inputLink.text.toString())
                    callJoshDownload()
                }
                binding.inputLink.text.contains("chingari") -> {
                    Log.e("Download called: ", binding.inputLink.text.toString())
                    callChingariDownload()
                }
                binding.inputLink.text.contains("twitter") -> {
                    Log.e("Download called: ", binding.inputLink.text.toString())
                    callTwitterDownload()
                }
                binding.inputLink.text.contains("share.mxtakatak.com") -> {
                    Log.e("Download called: ", binding.inputLink.text.toString())
                    callMxTakaTakDownload()
                }
                binding.inputLink.text.contains("likee") -> {
                    Log.e("Download called: ", binding.inputLink.text.toString())
                    callLikeeDownload()
                }
                binding.inputLink.text.toString().contains("tiktok") -> {
                    callTikTokDownload()
                }
                else -> Utils.createToast(applicationContext, "App not supported")
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

    private fun getUrlWithoutParameters(url: String): String {
        return try {
            val uri = URI(url)
            URI(
                uri.scheme,
                uri.authority,
                uri.path,
                null,  // Ignore the query part of the input url
                uri.fragment
            ).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            Utils.createToast(applicationContext, "Enter valid Url")
            ""
        }
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

    private fun getFileName(url: String): String {
        return if (url.contains(".mp4")) {
            "Insta: " + System.currentTimeMillis().toString() + ".mp4"
        } else {
            "Insta: " + System.currentTimeMillis().toString() + ".png"
        }
    }

    private fun callInstagramDownload() {
        val url = URL(binding.inputLink.text.toString())
        val host = url.host

        if (host == "www.instagram.com") {
            var urlWithoutQP =
                getUrlWithoutParameters(binding.inputLink.text.toString())

            Log.e(
                "userId", "ds_user_id=" + prefManager.getString(USER_ID)
                    .toString()
            )

            Log.e(
                "sessionId", "; sessionid=" + prefManager.getString(SESSION_ID)
            )

            urlWithoutQP = "$urlWithoutQP?__a=1"
            viewModel.getCallResultData(
                urlWithoutQP,
                "ds_user_id=" + prefManager.getString(USER_ID)
                    .toString() + "; sessionid=" + prefManager.getString(SESSION_ID)
            )
        }
    }

    private fun callFacebookDownload() {
        showProgressBar()
        viewModel.getFacebookData(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callLikeeDownload() {
        showProgressBar()
        viewModel.getLikeeData(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callRoposoDownload() {
        showProgressBar()
        viewModel.getRopossoData(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callMojDownload() {
        showProgressBar()
        viewModel.getMojData(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callMitronDownload() {
        showProgressBar()
        val split = (extractLinks(binding.inputLink.text.toString())).split("=")
        viewModel.getMitronData(arrayOf("https://web.mitron.tv/video/" + split[split.size - 1]))
    }

    private fun callTwitterDownload() {
        showProgressBar()
        viewModel.getTwitterData(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callMxTakaTakDownload() {
        showProgressBar()
        viewModel.getMxTakaTakData(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callChingariDownload() {
        showProgressBar()
        viewModel.getChingariData(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callJoshDownload() {
        showProgressBar()
        viewModel.getJoshData(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callShareChatDownload() {
        showProgressBar()
        viewModel.getShareChatData(extractLinks(binding.inputLink.text.toString()))
    }

    private fun callTikTokDownload() {
        viewModel.getTikTokData(extractLinks(binding.inputLink.text.toString()))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isEmpty()) {
            Snackbar.make(
                binding.root,
                "Read Permissions required to show downloaded files",
                Snackbar.LENGTH_LONG
            )
                .setAction("Settings") {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                        it.data = Uri.fromParts("package", "com.mystikcoder.statussaver", null)
                        startActivity(it)
                    }
                }.show()
        } else if (requestCode == 2) {
            if (grantResults.isEmpty()) {
                Snackbar.make(
                    binding.root,
                    "Storage Permissions required to download files",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Settings") {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                            it.data = Uri.fromParts("package", "com.mystikcoder.statussaver", null)
                            startActivity(it)
                        }
                    }.show()
            } else {
                Utils.createFileFolder()
            }
        }
    }

    override fun onBackPressed() {
        if (isFirstTimeClicked) {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
            isFirstTimeClicked = false
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
