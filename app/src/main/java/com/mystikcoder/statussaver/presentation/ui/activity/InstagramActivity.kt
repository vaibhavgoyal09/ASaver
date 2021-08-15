package com.mystikcoder.statussaver.presentation.ui.activity

import android.Manifest
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.ActivityInstagramBinding
import com.mystikcoder.statussaver.domain.events.common.DownloadRequestEvent
import com.mystikcoder.statussaver.domain.events.instagram.InstagramUserEvent
import com.mystikcoder.statussaver.domain.events.instagram.InstagramUserStoriesEvent
import com.mystikcoder.statussaver.domain.model.instagram.TrayModel
import com.mystikcoder.statussaver.extensions.*
import com.mystikcoder.statussaver.listeners.InstagramUserSelectedListener
import com.mystikcoder.statussaver.presentation.ui.adapters.StoryItemsAdapter
import com.mystikcoder.statussaver.presentation.ui.adapters.UsersListAdapter
import com.mystikcoder.statussaver.presentation.utils.DialogUtil
import com.mystikcoder.statussaver.presentation.utils.NetworkState
import com.mystikcoder.statussaver.presentation.utils.Utils
import com.mystikcoder.statussaver.presentation.viewmodel.InstagramViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class InstagramActivity : AppCompatActivity(), InstagramUserSelectedListener {

    private lateinit var binding: ActivityInstagramBinding
    private lateinit var clipboard: ClipboardManager
    private val viewModel: InstagramViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_instagram)

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        initViews()
        setupStateObservers()
    }

    private fun setupStateObservers() {

        lifecycleScope.launchWhenStarted {

            viewModel.userEvent.collect { event ->
                when (event) {
                    is InstagramUserEvent.Success -> {
                        val adapter = UsersListAdapter(
                            applicationContext,
                            event.data,
                            null,
                            null,
                            this@InstagramActivity,
                            "Instagram"
                        )
                    }
                    is InstagramUserEvent.Loading -> {

                    }
                    is InstagramUserEvent.Failure -> {

                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {

            viewModel.downloadEvent.collect { event ->
                when(event) {
                    is DownloadRequestEvent.Loading -> {
                        showProgressBar()
                    }
                    is DownloadRequestEvent.Error -> {
                        hideProgressBar()
                        applicationContext.showShortToast(event.errorMessage)
                    }
                    is DownloadRequestEvent.Success -> {
                        hideProgressBar()
                    }
                    else -> hideProgressBar()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.userStoriesEvent.collect { event ->
                when(event) {
                    is InstagramUserStoriesEvent.Success -> {
                        val adapter = StoryItemsAdapter(applicationContext , event.data , "Instagram" , null)
                    }
                    is InstagramUserStoriesEvent.Failure -> {
                        applicationContext.showShortToast(event.errorText)
                    }
                    is InstagramUserStoriesEvent.Loading -> {

                    }
                    else -> Unit
                }
            }
        }
    }

    private fun initViews() {
        val isInstaLoggedIn = viewModel.isLoggedIn()

        binding.imageInfo.setOnClickListener {
            DialogUtil.openBottomSheetDialog(this)
        }

        clipboard.getClipboardText(applicationContext)?.let {
            if (it.contains("instagram")) {
                binding.inputLink.setText(it)
            }
        }

        binding.switchFromPrivateAccount.setOnClickListener {
            if (!binding.switchFromPrivateAccount.isChecked) {
                if (isInstaLoggedIn) {
                    TODO()
                }
            } else {
                Intent(applicationContext, LoginActivity::class.java).also {
                    resultLauncher.launch(it)
                }
            }
        }

        binding.switchFromPrivateAccount.isChecked = isInstaLoggedIn

        if (!isInstaLoggedIn) {

            binding.storiesRecyclerView.visibility = View.GONE
            binding.storiesItemsRecyclerView.visibility = View.GONE

        } else {

            viewModel.getUsers()

        }

        binding.imageBack.setOnClickListener {
            onBackPressed()
        }

        binding.imageAppLogo.setOnClickListener {
            startAnotherApp("com.instagram.android", "com.instagram.lite")
        }

        binding.buttonPasteLink.setOnClickListener {
            binding.inputLink.setText(clipboard.getClipboardText(applicationContext))
        }

        binding.buttonDownload.setOnClickListener {
            if (NetworkState.isNetworkAvailable()) {
                if (binding.inputLink.text.toString().contains("instagram")) {
                    if (Build.VERSION.SDK_INT >= 29) {
                        setupListeners()
                    } else {
                        if (Utils.hasWritePermission(this)) {
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
                    applicationContext.showShortToast("Enter valid link")
                }
            } else {
                applicationContext.showShortToast("No Internet connection available")
            }
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

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.getUsers()
            }
        }

    private fun setupListeners() {
        viewModel.callDownload(binding.inputLink.text.toString())
    }

    override fun onResume() {
        super.onResume()
        clipboard.getClipboardText(this)?.let {
            if (it.contains("instagram")) {
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

    override fun onInstagramUserClicked(position: Int, trayModel: TrayModel) {

        if (NetworkState.isNetworkAvailable()) {
            binding.storiesItemsRecyclerView.visibility = View.GONE
            binding.loadingStoriesDataProgressBar.visibility = View.VISIBLE

            viewModel.getUserStories(trayModel.user.pk.toString())
        } else {
            applicationContext.showShortToast("No Internet connection available")
        }
    }
}