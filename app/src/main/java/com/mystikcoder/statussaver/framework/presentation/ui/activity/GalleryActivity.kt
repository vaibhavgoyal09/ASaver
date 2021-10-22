package com.mystikcoder.statussaver.framework.presentation.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.ActivityGalleryBinding
import com.mystikcoder.statussaver.framework.presentation.ui.adapters.FileListAdapter
import com.mystikcoder.statussaver.framework.presentation.ui.viewmodel.GalleryViewModel
import com.mystikcoder.statussaver.framework.utils.Utils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding
    private val viewModel: GalleryViewModel by viewModels()
    private lateinit var apps: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gallery)

        apps = arrayOf(
            "All",
            "Instagram",
            "Moj",
            "Mitron",
            "MxTakaTak",
            "Chingari",
            "Josh",
            "Roposo",
            "Likee",
            "Twitter",
            "ShareChat",
            "TikTok"
        )
        apps.sort() // Sort array alphabetically

        binding.appsSpinner.onItemSelectedListener = null
        checkPermissions()

        viewModel.hasPermissions.observe(this) {

            if (it) {

                binding.imageNoFileFound?.visibility = View.GONE
                binding.buttonAcceptPermissions?.visibility = View.GONE
                binding.textView?.visibility = View.GONE
                binding.savedItemsRecyclerView.visibility = View.VISIBLE
                binding.appsSpinner.visibility = View.VISIBLE

                binding.appsSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            viewModel.getSavedFiles(apps[position])
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            binding.appsSpinner.setSelection(0)
                            viewModel.getSavedFiles(apps[0])
                        }
                    }

                val adapter =
                    ArrayAdapter(
                        this,
                        R.layout.spinner_list_item,
                        apps
                    )
                binding.appsSpinner.adapter = adapter

            } else {

                binding.savedItemsRecyclerView.visibility = View.GONE
                binding.appsSpinner.visibility = View.GONE
                binding.imageNoFileFound?.visibility = View.VISIBLE
                binding.buttonAcceptPermissions?.visibility = View.VISIBLE
                binding.textView?.visibility = View.VISIBLE

            }
        }

        viewModel.savedFiles.observe(this) {
            binding.savedItemsRecyclerView.adapter = FileListAdapter(this, it)
            binding.textNoData?.visibility =
                if (it.toString().isEmpty()) View.VISIBLE else View.GONE
        }

        binding.imageBack.setOnClickListener {
            onBackPressed()
        }

        binding.buttonAcceptPermissions?.setOnClickListener {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                    it.data = Uri.fromParts("package", "com.mystikcoder.statussaver", null)
                    resultLauncher.launch(it)
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    101
                )
            }
        }
    }

    private fun checkPermissions() {
        if (!Utils.hasReadPermission(this)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                101
            )
        }
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Utils.hasReadPermission(this)) {
                viewModel.permissionsGranted()
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.permissionsGranted()
        }
    }
}
