package com.mystikcoder.statussaver.presentation.ui.activity

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
import androidx.lifecycle.lifecycleScope
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.ActivityGalleryBinding
import com.mystikcoder.statussaver.presentation.ui.adapters.FileListAdapter
import com.mystikcoder.statussaver.presentation.utils.Utils
import com.mystikcoder.statussaver.presentation.viewmodel.SavedFilesViewModel
import kotlinx.coroutines.launch

class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding
    private val viewModel: SavedFilesViewModel by viewModels()
    private lateinit var apps: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gallery)

        apps = arrayOf(
            "All",
            "Instagram",
            "Facebook",
            "WhatsApp",
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
        apps.sort()

        binding.appsSpinner.onItemSelectedListener = null
        checkPermissions()

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
            binding.savedItemsRecyclerView.visibility = View.GONE
            binding.appsSpinner.visibility = View.GONE
            binding.imageNoFileFound?.visibility = View.VISIBLE
            binding.buttonAcceptPermissions?.visibility = View.VISIBLE
            binding.textView?.visibility = View.VISIBLE
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                101
            )
        } else {
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
                        lifecycleScope.launch {
                            viewModel.getSavedFiles(this@GalleryActivity, apps[position])
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        binding.appsSpinner.setSelection(0)
                        lifecycleScope.launch {
                            viewModel.getSavedFiles(this@GalleryActivity, apps[0])
                        }
                    }
                }

            val adapter =
                ArrayAdapter(this, R.layout.spinner_list_item, apps)
            binding.appsSpinner.adapter = adapter
        }
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Utils.hasReadPermission(this)) {
                binding.appsSpinner.visibility = View.VISIBLE
                binding.appsSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            lifecycleScope.launch {
                                viewModel.getSavedFiles(this@GalleryActivity, apps[position])
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            binding.appsSpinner.setSelection(0)
                            lifecycleScope.launch {
                                viewModel.getSavedFiles(this@GalleryActivity, apps[0])
                            }
                        }
                    }

                val adapter =
                    ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, apps).also {
                        it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                binding.appsSpinner.adapter = adapter
            } else {
                binding.imageNoFileFound?.visibility = View.VISIBLE
                binding.buttonAcceptPermissions?.visibility = View.VISIBLE
                binding.textView?.visibility = View.VISIBLE
                binding.savedItemsRecyclerView.visibility = View.GONE
                binding.appsSpinner.visibility = View.GONE
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isEmpty()) {
            binding.imageNoFileFound?.visibility = View.VISIBLE
            binding.buttonAcceptPermissions?.visibility = View.VISIBLE
            binding.textView?.visibility = View.VISIBLE
            binding.savedItemsRecyclerView.visibility = View.GONE
            binding.appsSpinner.visibility = View.GONE
        } else if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                        lifecycleScope.launch {
                            viewModel.getSavedFiles(this@GalleryActivity, apps[position])
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        binding.appsSpinner.setSelection(0)
                        lifecycleScope.launch {
                            viewModel.getSavedFiles(this@GalleryActivity, apps[0])
                        }
                    }
                }

            val adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, apps).also {
                    it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
            binding.appsSpinner.adapter = adapter
        }
    }
}
