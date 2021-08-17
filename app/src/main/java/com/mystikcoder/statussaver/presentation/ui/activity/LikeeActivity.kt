package com.mystikcoder.statussaver.presentation.ui.activity

import android.Manifest
import android.content.ClipDescription
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
import com.mystikcoder.statussaver.databinding.ActivityLikeeBinding
import com.mystikcoder.statussaver.extensions.getClipboardText
import com.mystikcoder.statussaver.extensions.showRequestPermissionSnackbar
import com.mystikcoder.statussaver.extensions.showSettingsSnackbar
import com.mystikcoder.statussaver.extensions.showShortToast
import com.mystikcoder.statussaver.presentation.framework.events.common.DownloadRequestEvent
import com.mystikcoder.statussaver.presentation.ui.viewmodel.LikeeViewModel
import com.mystikcoder.statussaver.presentation.utils.DialogUtil
import com.mystikcoder.statussaver.presentation.utils.NetworkState
import com.mystikcoder.statussaver.presentation.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class LikeeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLikeeBinding
    private lateinit var clipboard: ClipboardManager
    private val viewModel: LikeeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_likee)

        initViews()
    }

    private fun initViews() {
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        binding.imageAppLogo.setOnClickListener {
//            Utils.openApp(applicationContext, "video.like")
        }
        binding.buttonPasteLink.setOnClickListener {
            if (clipboard.hasPrimaryClip()) {
                if (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)!!) {
                    binding.inputLink.setText(clipboard.primaryClip?.getItemAt(0)?.text.toString())
                }
            }
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
                    is DownloadRequestEvent.Error -> {
                        hideProgressBar()
                        applicationContext.showShortToast(event.errorMessage)
                    }
                    is DownloadRequestEvent.Success -> {
                        hideProgressBar()
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
            if (it.contains("likee")) {
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
