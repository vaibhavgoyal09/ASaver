package com.mystikcoder.statussaver.presentation.ui.activity

import android.Manifest
import android.content.*
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
import com.mystikcoder.statussaver.databinding.ActivityFacebookBinding
import com.mystikcoder.statussaver.domain.events.common.DownloadRequestEvent
import com.mystikcoder.statussaver.domain.events.facebook.FacebookUserEvent
import com.mystikcoder.statussaver.domain.events.facebook.FacebookUserStoriesEvent
import com.mystikcoder.statussaver.domain.model.facebook.FacebookNode
import com.mystikcoder.statussaver.extensions.getClipboardText
import com.mystikcoder.statussaver.extensions.showRequestPermissionSnackbar
import com.mystikcoder.statussaver.extensions.showSettingsSnackbar
import com.mystikcoder.statussaver.extensions.showShortToast
import com.mystikcoder.statussaver.listeners.FacebookUserSelectedListener
import com.mystikcoder.statussaver.presentation.ui.adapters.StoryItemsAdapter
import com.mystikcoder.statussaver.presentation.ui.adapters.UsersListAdapter
import com.mystikcoder.statussaver.presentation.utils.*
import com.mystikcoder.statussaver.presentation.viewmodel.FacebookViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class FacebookActivity : AppCompatActivity(), FacebookUserSelectedListener {

    private lateinit var binding: ActivityFacebookBinding
    private lateinit var clipboard: ClipboardManager

    private val viewModel: FacebookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_facebook)
        Utils.hasWritePermission(applicationContext)
        initViews()
    }

    private fun initViews() {
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val isLoggedIn = viewModel.isLoggedIn()

        binding.imageInfo.setOnClickListener {
            DialogUtil.openBottomSheetDialog(this)
        }

        clipboard.getClipboardText(applicationContext)?.let {
            if (it.contains("facebook") || it.contains("fb")) {
                binding.inputLink.setText(it)
            }
        }

        binding.switchFromPrivateAccount.setOnClickListener {
            if (!binding.switchFromPrivateAccount.isChecked) {
                if (isLoggedIn) {
                    TODO()
                }
            } else {
                Intent(applicationContext, FacebookLoginActivity::class.java).also {
                    resultLauncher.launch(it)
                }
            }
        }

        binding.switchFromPrivateAccount.isChecked = isLoggedIn

        if (isLoggedIn) {
            viewModel.getUsers()
        }

        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        binding.imageAppLogo.setOnClickListener {

        }
        binding.buttonPasteLink.setOnClickListener {
            binding.inputLink.setText(clipboard.getClipboardText(applicationContext))
        }
        binding.buttonDownload.setOnClickListener {

            if (Build.VERSION.SDK_INT >= 29) {
                startDownload()
            } else {
                if (Utils.hasWritePermission(applicationContext)) {
                    startDownload()
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        binding.root.showRequestPermissionSnackbar(this)
                    } else {
                        binding.root.showSettingsSnackbar(this)
                    }
                }
            }

        }

        lifecycleScope.launchWhenStarted {

            viewModel.downloadState.collect { event ->
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

        lifecycleScope.launchWhenStarted {
            viewModel.userState.collect { event ->
                when (event) {
                    is FacebookUserEvent.Success -> {

                        val adapter = UsersListAdapter(
                            applicationContext,
                            null,
                            event.list,
                            this@FacebookActivity,
                            null,
                            "Facebook"
                        )

                        binding.loadingStoriesProgressBar.visibility = View.GONE
                        binding.storiesRecyclerView.adapter = adapter
                        binding.storiesRecyclerView.visibility = View.VISIBLE
                    }
                    is FacebookUserEvent.Failure -> {
                        binding.loadingStoriesProgressBar.visibility = View.GONE
                        showShortToast(event.errorText)
                    }
                    is FacebookUserEvent.Loading -> {
                        binding.loadingStoriesProgressBar.visibility = View.VISIBLE
                    }
                    else -> binding.loadingStoriesProgressBar.isVisible = false
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.userStoriesState.collect { event ->
                when (event) {
                    is FacebookUserStoriesEvent.Success -> {

                        binding.loadingStoriesDataProgressBar.isVisible = false
                        binding.storiesItemsRecyclerView.visibility = View.VISIBLE

                        val adapter =
                            StoryItemsAdapter(applicationContext, null, "Facebook", event.list)
                        binding.storiesItemsRecyclerView.adapter = adapter

                    }
                    is FacebookUserStoriesEvent.Failure -> {
                        binding.loadingStoriesDataProgressBar.isVisible = false
                        binding.storiesItemsRecyclerView.isVisible = false
                       applicationContext.showShortToast(event.errorText)
                    }
                    else -> binding.loadingStoriesDataProgressBar.isVisible = false
                }
            }
        }
    }

    private fun startDownload() {
        viewModel.downloadFile(binding.inputLink.text.toString())
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

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (viewModel.isLoggedIn()) {
                binding.loadingStoriesProgressBar.isVisible = true
                binding.switchFromPrivateAccount.isChecked = true

            } else {
                binding.switchFromPrivateAccount.isChecked = false
            }
        }

    override fun onResume() {
        super.onResume()
        clipboard.getClipboardText(applicationContext)?.let {
            if (it.contains("facebook") || it.contains("fb")) {
                binding.inputLink.setText(it)
            }
        }
    }

    override fun onFacebookUserClicked(position: Int, nodeModel: FacebookNode) {
        binding.loadingStoriesDataProgressBar.isVisible = true
        binding.storiesItemsRecyclerView.visibility = View.GONE
        viewModel.getUserStories(nodeModel.nodeData.id)
    }

    override fun onBackPressed() {
        if (DialogUtil.isSheetShowing()) {
            DialogUtil.hideSheet()
            return
        } else {
            super.onBackPressed()
        }
    }
}