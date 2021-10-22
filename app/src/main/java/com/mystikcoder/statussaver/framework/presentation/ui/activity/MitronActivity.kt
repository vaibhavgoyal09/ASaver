package com.mystikcoder.statussaver.framework.presentation.ui.activity

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.ActivityMitronBinding
import com.mystikcoder.statussaver.framework.events.common.DownloadRequestEvent
import com.mystikcoder.statussaver.framework.extensions.*
import com.mystikcoder.statussaver.framework.presentation.ui.viewmodel.MitronViewModel
import com.mystikcoder.statussaver.framework.utils.DialogUtil
import com.mystikcoder.statussaver.framework.utils.NetworkState
import com.mystikcoder.statussaver.framework.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MitronActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMitronBinding
    private lateinit var clipboard: ClipboardManager
    private val viewModel: MitronViewModel by viewModels()

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
            startAnotherApp("com.mitron.tv")
        }

        binding.buttonPasteLink.setOnClickListener {
            binding.inputLink.setText(clipboard.getClipboardText(applicationContext))
        }

        binding.imageInfo.setOnClickListener {
            DialogUtil.openBottomSheetDialog(this)
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

        lifecycleScope.launchWhenStarted {
            viewModel.downloadEvent.collect { event ->
                when (event) {
                    is DownloadRequestEvent.Success -> {
                        hideProgressBar()
                    }
                    is DownloadRequestEvent.Error -> {
                        hideProgressBar()
                        applicationContext.showShortToast(event.errorMessage)
                    }
                    is DownloadRequestEvent.Loading -> {
                        showProgressBar()
                    }
                    else -> hideProgressBar()
                }
            }
        }
    }

    private fun setupListeners() {
        val split = binding.inputLink.text.split("=")
        viewModel.download(arrayOf("https://web.mitron.tv/video/" + split[split.size - 1])[0])
    }

    override fun onResume() {
        super.onResume()
        clipboard.getClipboardText(applicationContext)?.let {
            if (it.contains("mitron")) {
                binding.inputLink.setText(it)
            }
        }
    }

    override fun onBackPressed() {
        if (DialogUtil.isSheetShowing()) {
            DialogUtil.hideSheet()
            return
        } else {
            super.onBackPressed()
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