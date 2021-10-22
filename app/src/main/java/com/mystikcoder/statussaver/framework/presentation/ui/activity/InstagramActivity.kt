package com.mystikcoder.statussaver.framework.presentation.ui.activity

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
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.core.domain.model.instagram.TrayModel
import com.mystikcoder.statussaver.databinding.ActivityInstagramBinding
import com.mystikcoder.statussaver.framework.events.common.DownloadRequestEvent
import com.mystikcoder.statussaver.framework.events.instagram.InstagramUserEvent
import com.mystikcoder.statussaver.framework.events.instagram.InstagramUserStoriesEvent
import com.mystikcoder.statussaver.framework.extensions.*
import com.mystikcoder.statussaver.framework.listeners.InstagramUserSelectedListener
import com.mystikcoder.statussaver.framework.presentation.ui.adapters.StoryItemsAdapter
import com.mystikcoder.statussaver.framework.presentation.ui.adapters.UsersListAdapter
import com.mystikcoder.statussaver.framework.presentation.ui.fragment.LogOutDialogFragment
import com.mystikcoder.statussaver.framework.utils.DialogUtil
import com.mystikcoder.statussaver.framework.utils.NetworkState
import com.mystikcoder.statussaver.framework.utils.Utils
import com.mystikcoder.statussaver.framework.presentation.ui.viewmodel.InstagramViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class InstagramActivity : AppCompatActivity(), InstagramUserSelectedListener {

    private lateinit var binding: ActivityInstagramBinding
    private lateinit var clipboard: ClipboardManager
    private val viewModel: InstagramViewModel by viewModels()

    companion object {
        const val LOG_OUT_DIALOG_TAG = "tag_log_out_dialog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_instagram)

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        if (savedInstanceState != null) {
            val fragment =
                supportFragmentManager.findFragmentByTag(LOG_OUT_DIALOG_TAG) as LogOutDialogFragment?
            fragment?.initDialog(
                resources.getString(R.string.confirm_log_out_message_insta),
                logOutListener = {
                    viewModel.logOut()
                },
                cancelListener = {
                    binding.switchFromPrivateAccount.isChecked = true
                })
        }

        initViews()
        setupStateObservers()
    }

    private fun setupStateObservers() {

        viewModel.isLoggedIn.observe(this) {
            binding.switchFromPrivateAccount.isChecked = it
            binding.storiesRecyclerView.isVisible = it
        }

        lifecycleScope.launchWhenStarted {

            viewModel.userEvent.collect { event ->
                when (event) {
                    is InstagramUserEvent.Success -> {
                        hideLoadingUsersDialog()
                        val adapter = UsersListAdapter(
                            applicationContext,
                            event.data,
                            null,
                            null,
                            this@InstagramActivity,
                            "Instagram"
                        )
                        binding.storiesRecyclerView.adapter = adapter
                    }
                    is InstagramUserEvent.Loading -> {
                        showLoadingUsersDialog()
                    }
                    is InstagramUserEvent.Failure -> {
                        hideLoadingUsersDialog()
                        applicationContext.showShortToast(event.errorText)
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {

            viewModel.downloadEvent.collect { event ->
                when (event) {
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
                when (event) {
                    is InstagramUserStoriesEvent.Success -> {
                        hideLoadingStoriesDialog()
                        val adapter =
                            StoryItemsAdapter(applicationContext, event.data, "Instagram", null)
                        binding.storiesItemsRecyclerView.adapter = adapter
                    }
                    is InstagramUserStoriesEvent.Failure -> {
                        hideLoadingStoriesDialog()
                        applicationContext.showShortToast(event.errorText)
                    }
                    is InstagramUserStoriesEvent.Loading -> {
                        showLoadingStoriesDialog()
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
                    LogOutDialogFragment().apply {
                        initDialog(
                            applicationContext.resources.getString(R.string.confirm_log_out_message_insta),
                            logOutListener = {
                                viewModel.logOut()
                            },
                            cancelListener = {
                                binding.switchFromPrivateAccount.isChecked = true
                            }
                        )
                    }.show(supportFragmentManager, LOG_OUT_DIALOG_TAG)
                }
            } else {
                Intent(applicationContext, LoginActivity::class.java).also {
                    resultLauncher.launch(it)
                }
            }
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
                viewModel.logIn()
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
        binding.switchFromPrivateAccount.isChecked = viewModel.isLoggedIn()
    }

    override fun onBackPressed() {
        if (DialogUtil.isSheetShowing()) {
            DialogUtil.hideSheet()
            return
        } else {
            super.onBackPressed()
        }
    }

    private fun hideLoadingStoriesDialog() {
        binding.loadingStoriesDataProgressBar.visibility = View.GONE
        binding.storiesItemsRecyclerView.visibility = View.VISIBLE
    }

    private fun showLoadingStoriesDialog() {
        binding.loadingStoriesDataProgressBar.visibility = View.VISIBLE
        binding.storiesItemsRecyclerView.visibility = View.GONE
    }

    private fun showLoadingUsersDialog() {
        binding.loadingStoriesProgressBar.visibility = View.VISIBLE
        binding.storiesRecyclerView.visibility = View.GONE
    }

    private fun hideLoadingUsersDialog() {
        binding.loadingStoriesProgressBar.visibility = View.GONE
        binding.storiesRecyclerView.visibility = View.VISIBLE
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