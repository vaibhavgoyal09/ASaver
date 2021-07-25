package com.mystikcoder.statussaver.ui.activity

import android.Manifest
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Patterns
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.ActivityMitronBinding
import com.mystikcoder.statussaver.states.MitronEvent
import com.mystikcoder.statussaver.utils.Utils
import com.mystikcoder.statussaver.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MitronActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMitronBinding
    private lateinit var clipboard: ClipboardManager
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mitron)
        initViews()
    }

    private fun initViews() {
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        binding.imageAppLogo.setOnClickListener {
            Utils.openApp(applicationContext, "com.mitron.tv")
        }
        binding.buttonPasteLink.setOnClickListener {
            if (clipboard.hasPrimaryClip()) {
                if (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)!!) {
                    binding.inputLink.setText(clipboard.primaryClip?.getItemAt(0)?.text.toString())
                }
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
                                    it.data =
                                        Uri.fromParts(
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
    }

    private fun setupListeners() {
        val url: String = binding.inputLink.text.toString()
        if (url == "") {
            Utils.createToast(this, "Enter URL")
        } else if (!Patterns.WEB_URL.matcher(url).matches() || !url.contains("mitron")) {
            Utils.createToast(this, "Enter valid URL")
        } else {
            showProgressBar()
            val split = binding.inputLink.text.split("=")
            viewModel.getMitronData(arrayOf("https://web.mitron.tv/video/" + split[split.size - 1]))
        }
    }

    override fun onResume() {
        super.onResume()
        Utils.hasWritePermission(applicationContext)
        if (clipboard.hasPrimaryClip()) {
            if (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)!!) {
                if (clipboard.primaryClip?.getItemAt(0)?.text.toString().contains("mitron")) {
                    binding.inputLink.setText(clipboard.primaryClip?.getItemAt(0)?.text.toString())
                }
            }
        }
    }

    private fun showProgressBar() {
        binding.buttonDownload.visibility = View.GONE
        binding.buttonPasteLink.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.buttonDownload.visibility = View.VISIBLE
        binding.buttonPasteLink.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }
}