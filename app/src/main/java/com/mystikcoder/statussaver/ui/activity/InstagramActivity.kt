package com.mystikcoder.statussaver.ui.activity

import android.Manifest
import android.app.Activity
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.adapters.StoryItemsAdapter
import com.mystikcoder.statussaver.adapters.UsersListAdapter
import com.mystikcoder.statussaver.databinding.ActivityInstagramBinding
import com.mystikcoder.statussaver.listeners.UserSelectedListener
import com.mystikcoder.statussaver.model.facebook.FacebookNode
import com.mystikcoder.statussaver.model.instagram.TrayModel
import com.mystikcoder.statussaver.states.instagram.InstagramEvent
import com.mystikcoder.statussaver.states.instagram.InstagramStoryDetailEvent
import com.mystikcoder.statussaver.states.instagram.InstagramStoryEvent
import com.mystikcoder.statussaver.utils.*
import com.mystikcoder.statussaver.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.net.URI
import javax.inject.Inject

@AndroidEntryPoint
class InstagramActivity : AppCompatActivity(), UserSelectedListener {

    @Inject
    lateinit var prefManager: PrefManager

    private lateinit var binding: ActivityInstagramBinding
    private lateinit var clipboard: ClipboardManager
    private val viewModel: MainViewModel by viewModels()
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_instagram)

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        initViews()
    }

    private fun initViews() {
        val isInstaLoggedIn = prefManager.getBoolean(IS_INSTA_LOGGED_IN)

        binding.inputLink.setText(intent?.getStringExtra("CopyIntent"))

        if (clipboard.hasPrimaryClip()) {
            if (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)!!) {
                if (clipboard.primaryClip?.getItemAt(0)?.text.toString().contains("instagram")) {
                    binding.inputLink.setText(clipboard.primaryClip?.getItemAt(0)?.text.toString())
                }
            }
        }

        binding.switchFromPrivateAccount.setOnClickListener {
            if (!binding.switchFromPrivateAccount.isChecked) {
                if (isInstaLoggedIn) {
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
                            prefManager.putBoolean(IS_INSTA_LOGGED_IN, false)
                            binding.storiesItemsRecyclerView.visibility = View.GONE
                            binding.storiesRecyclerView.visibility = View.GONE
                            binding.switchFromPrivateAccount.isChecked = false
                            prefManager.clearInstagramPrefs()

                            alertDialog?.dismiss()
                        }
                        view.findViewById<TextView>(R.id.textMessage).text = applicationContext.resources.getString(R.string.confirm_log_out_message_insta)
                        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(0))
                        alertDialog?.window?.attributes?.windowAnimations = R.style.AlertDialogAnimation
                    }
                    alertDialog?.show()
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
            if (Utils.isNetworkAvailable(applicationContext)) {
                try {
                    viewModel.getStories(
                        "ds_user_id=" + prefManager.getString(USER_ID)
                            .toString() + "; sessionid=" + prefManager.getString(SESSION_ID)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Utils.createToast(applicationContext, "No Internet connection available")
            }
        }
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        binding.imageAppLogo.setOnClickListener {
            Utils.openApp(applicationContext, "com.instagram.android")
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
                    Utils.createToast(this, "Enter valid link")
                }
            } else {
                Utils.createToast(applicationContext, "No Internet connection available")
            }
        }

        lifecycleScope.launchWhenStarted {

            viewModel.instagramStoriesData.collect { storiesEvent ->
                when (storiesEvent) {
                    is InstagramStoryEvent.Failure -> {
                        binding.loadingStoriesProgressBar.visibility = View.GONE
                        Utils.createToast(applicationContext, storiesEvent.errorText)
                    }
                    is InstagramStoryEvent.Success -> {

                        storiesEvent.data?.let {
                            binding.loadingStoriesProgressBar.visibility = View.GONE
                            if (storiesEvent.data.size > 0) {
                                val usersStoriesAdapter = UsersListAdapter(
                                    applicationContext,
                                    storiesEvent.data,
                                    null,
                                    this@InstagramActivity,
                                    "Instagram"
                                )
                                binding.storiesRecyclerView.adapter = usersStoriesAdapter
                                binding.storiesRecyclerView.visibility = View.VISIBLE
                            }
                        }
                    }
                    is InstagramStoryEvent.Loading -> {
                        binding.loadingStoriesProgressBar.visibility = View.VISIBLE
                    }

                    else -> {
                        binding.storiesRecyclerView.visibility = View.GONE
                        binding.storiesItemsRecyclerView.visibility = View.GONE
                        binding.loadingStoriesProgressBar.visibility = View.GONE
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.instagramData.collect { resultDataEvent ->
                when (resultDataEvent) {
                    is InstagramEvent.Loading -> {
                        showProgressBar()
                    }
                    is InstagramEvent.Success -> {
                        hideProgressBar()

                        resultDataEvent.mediaUrl?.let {
                            Utils.createToast(applicationContext, "Download started")

                            for (downloadUrl in resultDataEvent.mediaUrl) {
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
                        Utils.createToast(applicationContext, resultDataEvent.errorText)
                    }
                    else -> hideProgressBar()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.instagramStoriesDetailsData.collect { storiesDetailEvent ->
                when (storiesDetailEvent) {
                    is InstagramStoryDetailEvent.Failure -> {
                        binding.loadingStoriesDataProgressBar.visibility = View.GONE
                        binding.storiesItemsRecyclerView.visibility = View.GONE
                        Utils.createToast(applicationContext, storiesDetailEvent.errorText)
                    }
                    is InstagramStoryDetailEvent.Loading -> {
                        binding.loadingStoriesDataProgressBar.visibility = View.VISIBLE
                    }
                    is InstagramStoryDetailEvent.Success -> {
                        binding.loadingStoriesDataProgressBar.visibility = View.GONE

                        storiesDetailEvent.data?.let {
                            if (storiesDetailEvent.data.size > 0) {
                                binding.storiesItemsRecyclerView.visibility = View.VISIBLE

                                val storiesDataAdapter =
                                    StoryItemsAdapter(
                                        applicationContext,
                                        storiesDetailEvent.data,
                                        "Instagram",
                                        null
                                    )
                                binding.storiesItemsRecyclerView.adapter = storiesDataAdapter

                            } else {
                                binding.storiesItemsRecyclerView.visibility = View.GONE
                            }
                        }
                    }
                    else -> binding.storiesItemsRecyclerView.visibility = View.GONE
                }
            }
        }
    }

    private fun getFileName(url: String): String {
        return if (url.contains(".mp4")) {
            "Insta: " + Math.random().toLong() + ".mp4"
        } else {
            "Insta: " + Math.random().toLong() + ".png"
        }
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

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                binding.switchFromPrivateAccount.isChecked = true

                if (Utils.isNetworkAvailable(applicationContext)) {
                    try {
                        viewModel.getStories(
                            "ds_user_id=" + prefManager.getString(USER_ID) + "; sessionid=" + prefManager.getString(
                                SESSION_ID
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                binding.switchFromPrivateAccount.isChecked = false
            }
        }

    private fun setupListeners() {
        val link: String = binding.inputLink.text.toString()
        if (link.isEmpty()) {
            Utils.createToast(applicationContext, "Enter Url")
        } else if (!Patterns.WEB_URL.matcher(link).matches() || !link.contains("instagram")) {
            Utils.createToast(applicationContext, "Enter valid Url")
        } else {
            try {

                var urlWithoutQP =
                    getUrlWithoutParameters(binding.inputLink.text.toString())

                urlWithoutQP = "$urlWithoutQP$INSTAGRAM_PARAMATERS"
                viewModel.getCallResultData(
                    urlWithoutQP,
                    "ds_user_id=" + prefManager.getString(USER_ID)
                        .toString() + "; sessionid=" + prefManager.getString(SESSION_ID)
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Utils.hasWritePermission(applicationContext)
        if (clipboard.hasPrimaryClip()) {
            if (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)!!) {
                if (clipboard.primaryClip?.getItemAt(0)?.text.toString().contains("instagram")) {
                    binding.inputLink.setText(clipboard.primaryClip?.getItemAt(0)?.text.toString())
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (alertDialog != null && alertDialog!!.isShowing){
            alertDialog = null
        }
    }

    override fun onInstagramUserClicked(position: Int, trayModel: TrayModel) {

        if (Utils.isNetworkAvailable(applicationContext)) {
            binding.storiesItemsRecyclerView.visibility = View.GONE
            binding.loadingStoriesDataProgressBar.visibility = View.VISIBLE

            viewModel.getStoryDetails(
                "ds_user_id=" + prefManager.getString(USER_ID)
                    .toString() + "; sessionid=" + prefManager.getString(SESSION_ID),
                trayModel.user.pk.toString()
            )
        } else {
            Utils.createToast(applicationContext, "No Internet connection available")
        }
    }

    override fun onFacebookUserClicked(position: Int, nodeModel: FacebookNode) {
    }

    override fun onDestroy() {
        super.onDestroy()
        if (alertDialog != null && alertDialog!!.isShowing){
            alertDialog = null
        }
    }
}