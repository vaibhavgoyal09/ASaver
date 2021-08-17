package com.mystikcoder.statussaver.presentation.ui.activity

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
import com.mystikcoder.statussaver.databinding.ActivityMxTakaTakBinding
import com.mystikcoder.statussaver.extensions.*
import com.mystikcoder.statussaver.presentation.framework.events.common.DownloadRequestEvent
import com.mystikcoder.statussaver.presentation.ui.viewmodel.MxTakaTakViewModel
import com.mystikcoder.statussaver.presentation.utils.DialogUtil
import com.mystikcoder.statussaver.presentation.utils.NetworkState
import com.mystikcoder.statussaver.presentation.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MxTakaTakActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMxTakaTakBinding
    private lateinit var clipboard: ClipboardManager
    private val viewModel: MxTakaTakViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mx_taka_tak)
        initViews()
    }

    private fun initViews() {
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        binding.imageAppLogo.setOnClickListener {
            startAnotherApp("com.next.innovation.takatak")
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
                }else{
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

    private fun setupListeners() {
        viewModel.download(binding.inputLink.text.toString())
    }

    override fun onBackPressed() {
        if (DialogUtil.isSheetShowing()){
            DialogUtil.hideSheet()
            return
        }else{
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        clipboard.getClipboardText(applicationContext)?.let {
            if (it.contains("takatak")) {
                binding.inputLink.setText(it)
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