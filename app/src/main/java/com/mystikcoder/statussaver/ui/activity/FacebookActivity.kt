package com.mystikcoder.statussaver.ui.activity

import android.Manifest
import android.content.*
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.mystikcoder.statussaver.databinding.ActivityFacebookBinding
import com.google.android.material.snackbar.Snackbar
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.adapters.StoryItemsAdapter
import com.mystikcoder.statussaver.adapters.UsersListAdapter
import com.mystikcoder.statussaver.listeners.UserSelectedListener
import com.mystikcoder.statussaver.model.facebook.FacebookNode
import com.mystikcoder.statussaver.model.instagram.TrayModel
import com.mystikcoder.statussaver.utils.*
import com.mystikcoder.statussaver.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class FacebookActivity : AppCompatActivity(), UserSelectedListener {

    private lateinit var binding: ActivityFacebookBinding
    private lateinit var clipboard: ClipboardManager

    @Inject
    lateinit var prefManager: PrefManager

    private val viewModel: MainViewModel by viewModels()

    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_facebook)
        Utils.hasWritePermission(applicationContext)
        initViews()
    }

    private fun initViews() {
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        if (clipboard.hasPrimaryClip()) {
            if (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)!!) {
                if (clipboard.primaryClip?.getItemAt(0)?.text.toString().contains("facebook")) {
                    binding.inputLink.setText(clipboard.primaryClip?.getItemAt(0)?.text.toString())
                }
            }
        }

        val isFacebookLoggedIn = prefManager.getBoolean(IS_FB_LOGGED_IN)

        binding.switchFromPrivateAccount.setOnClickListener {
            if (!binding.switchFromPrivateAccount.isChecked) {
                if (isFacebookLoggedIn) {
                    if (alertDialog == null) {

                        val view = LayoutInflater.from(this).inflate(
                            R.layout.log_out_alert_dilaog,
                            findViewById(R.id.layoutAlertDialog)
                        )

                        val dialogBuilder = AlertDialog.Builder(this)
                            .setView(view)

                        alertDialog = dialogBuilder.create()

                        view.findViewById<TextView>(R.id.textCancel).setOnClickListener {
                            binding.switchFromPrivateAccount.isChecked = true
                            alertDialog?.dismiss()
                        }
                        view.findViewById<TextView>(R.id.textLogOut).setOnClickListener {
                            prefManager.putBoolean(IS_FB_LOGGED_IN, false)
                            binding.storiesItemsRecyclerView.visibility = View.GONE
                            binding.storiesRecyclerView.visibility = View.GONE
                            binding.switchFromPrivateAccount.isChecked = false
                            prefManager.clearFacebookPrefs()

                            alertDialog?.dismiss()
                        }
                        view.findViewById<TextView>(R.id.textMessage).text = applicationContext.resources.getString(R.string.confirm_log_out_message_fb)
                        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(0))
                        alertDialog?.window?.attributes?.windowAnimations = R.style.AlertDialogAnimation
                    }
                    alertDialog?.show()
                }
            } else {
                Intent(applicationContext, FacebookLoginActivity::class.java).also {
                    resultLauncher.launch(it)
                }
            }
        }

        binding.switchFromPrivateAccount.isChecked = isFacebookLoggedIn

        if (isFacebookLoggedIn) {

            if (Utils.isNetworkAvailable(applicationContext)) {

                viewModel.getFacebookUserData(
                    prefManager.getString(FB_COOKIES)!!, prefManager.getString(FB_KEY)!!
                )
            } else {
                Utils.createToast(applicationContext, "No Internet connection available")
            }
        }

        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        binding.imageAppLogo.setOnClickListener {
            Utils.openApp(applicationContext, "com.facebook.katana")
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

            viewModel.facebookData.collect { event ->
                when (event) {
                    is MainViewModel.FacebookEvent.Success -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, "Download started")
                        Utils.startDownload(
                            event.videoUrl,
                            Utils.ROOT_DIRECTORY_FACEBOOK,
                            applicationContext,
                            event.fileName
                        )
                    }
                    is MainViewModel.FacebookEvent.Failure -> {
                        hideProgressBar()
                        Utils.createToast(applicationContext, event.errorText)
                    }
                    else -> hideProgressBar()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.usersDataList.collect { event ->
                when (event) {
                    is MainViewModel.UsersDataEvent.Success -> {

                        val adapter = UsersListAdapter(
                            applicationContext,
                            null,
                            event.list,
                            this@FacebookActivity,
                            "Facebook"
                        )

                        binding.loadingStoriesProgressBar.visibility = View.GONE
                        binding.storiesRecyclerView.adapter = adapter
                        binding.storiesRecyclerView.visibility = View.VISIBLE
                    }
                    is MainViewModel.UsersDataEvent.Failure -> {
                        binding.loadingStoriesProgressBar.visibility = View.GONE
                        Utils.createToast(applicationContext, event.errorText)
                    }
                    is MainViewModel.UsersDataEvent.Loading -> {
                        binding.loadingStoriesProgressBar.visibility = View.VISIBLE
                    }
                    else -> binding.loadingStoriesProgressBar.isVisible = false
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.facebookStoriesData.collect { event ->
                when (event) {
                    is MainViewModel.StoriesDataEvent.Success -> {

                        binding.loadingStoriesDataProgressBar.isVisible = false
                        binding.storiesItemsRecyclerView.visibility = View.VISIBLE

                        val adapter =
                            StoryItemsAdapter(applicationContext, null, "Facebook", event.list)
                        binding.storiesItemsRecyclerView.adapter = adapter

                    }
                    is MainViewModel.StoriesDataEvent.Failure -> {
                        binding.loadingStoriesDataProgressBar.isVisible = false
                        binding.storiesItemsRecyclerView.isVisible = false
                        Utils.createToast(applicationContext, event.errorText)
                    }
                    else -> binding.loadingStoriesDataProgressBar.isVisible = false
                }
            }
        }
    }

    private fun setupListeners() {
        val url: String = binding.inputLink.text.toString()
        if (url == "") {
            Utils.createToast(this, "Enter URL")
        } else if (!Patterns.WEB_URL.matcher(url).matches() || !url.contains("facebook")) {
            Utils.createToast(this, "Enter valid URL")
        } else {
            showProgressBar()
            viewModel.getFacebookData(url)
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

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (prefManager.getBoolean(IS_FB_LOGGED_IN)) {
                binding.loadingStoriesProgressBar.isVisible = true
                binding.switchFromPrivateAccount.isChecked = true

                if (Utils.isNetworkAvailable(applicationContext)) {
                    viewModel.getFacebookUserData(
                        prefManager.getString(FB_COOKIES)!!, prefManager.getString(FB_KEY)!!
                    )
                } else {
                    Utils.createToast(applicationContext, "No Internet connection available")
                }
            } else {
                binding.switchFromPrivateAccount.isChecked = false
            }
        }

    override fun onResume() {
        super.onResume()
        if (clipboard.hasPrimaryClip()) {
            if (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)!!) {
                if (clipboard.primaryClip?.getItemAt(0)?.text.toString().contains("facebook")) {
                    binding.inputLink.setText(clipboard.primaryClip?.getItemAt(0)?.text.toString())
                }
            }
        }
    }

    override fun onInstagramUserClicked(position: Int, trayModel: TrayModel) {}

    override fun onFacebookUserClicked(position: Int, nodeModel: FacebookNode) {
        if (Utils.isNetworkAvailable(applicationContext)) {
            binding.loadingStoriesDataProgressBar.isVisible = true
            binding.storiesItemsRecyclerView.visibility = View.GONE
            viewModel.getStories(
                prefManager.getString(FB_COOKIES)!!,
                prefManager.getString(FB_KEY)!!,
                nodeModel.nodeData.id
            )
        } else {
            Utils.createToast(applicationContext, "No Internet connection available")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (alertDialog != null && alertDialog!!.isShowing){
            alertDialog = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (alertDialog != null && alertDialog!!.isShowing){
            alertDialog = null
        }
    }
}