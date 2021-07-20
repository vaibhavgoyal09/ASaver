package com.mystikcoder.statussaver.viewmodel

import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mystikcoder.statussaver.model.whatsapp.WhatsAppModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Suppress("Deprecation")
class WhatsAppViewModel : ViewModel() {

    private val _imagesData = MutableLiveData<ArrayList<WhatsAppModel>>()
    val imagesData: LiveData<ArrayList<WhatsAppModel>> = _imagesData

    private val _videosData = MutableLiveData<ArrayList<WhatsAppModel>>()
    val videoData: LiveData<ArrayList<WhatsAppModel>> = _videosData

    private val targetPath: String =
        Environment.getExternalStorageDirectory().absolutePath + "/WhatsApp/Media/.Statuses"

    private val targetPathBusiness =
        Environment.getExternalStorageDirectory().absolutePath + "/WhatsApp Business/Media/.Statuses"

    private lateinit var whatsAppModel: WhatsAppModel

    fun getImages() {

        val tempListFiles: ArrayList<WhatsAppModel> = ArrayList()

        val directory = File(targetPath)
        val allFiles = directory.listFiles()

        val directoryBusiness = File(targetPathBusiness)
        val allFilesBusiness = directoryBusiness.listFiles()

        CoroutineScope(Dispatchers.IO).launch {
            if (!allFiles.isNullOrEmpty()) {

                for (file in allFiles) {

                    if (Uri.fromFile(file).toString().endsWith(".png") || Uri.fromFile(file)
                            .toString()
                            .endsWith(".jpg")
                    ) {

                        whatsAppModel = WhatsAppModel(
                            name = "WhatsStatus: " + System.currentTimeMillis().toString(),
                            uri = Uri.fromFile(file),
                            path = file.absolutePath,
                            fileName = file.name
                        )
                        tempListFiles.add(whatsAppModel)
                        Log.e("Whatsapp data", tempListFiles.toString())
                    }
                }
            }
            if (!allFilesBusiness.isNullOrEmpty()) {

                for (file in allFilesBusiness) {

                    if (Uri.fromFile(file).toString().endsWith(".png") || Uri.fromFile(file)
                            .toString()
                            .endsWith(".jpg")
                    ) {
                        whatsAppModel = WhatsAppModel(
                            name = "WhatsStatusBusiness: " + System.currentTimeMillis().toString(),
                            uri = Uri.fromFile(file),
                            path = file.absolutePath,
                            fileName = file.name
                        )
                        tempListFiles.add(whatsAppModel)
                        Log.e("Whatsapp data", tempListFiles.toString())
                    }
                }
            }
        }
        _imagesData.value = tempListFiles
//        Log.e("Whatsapp data", tempListFiles.toString())
    }

    fun getVideos() {
        val tempFilesList: ArrayList<WhatsAppModel> = ArrayList()

        val directory = File(targetPath)
        val allFiles = directory.listFiles()

        val directoryBusiness = File(targetPathBusiness)
        val allFilesBusiness = directoryBusiness.listFiles()

        CoroutineScope(Dispatchers.IO).launch {
            if (!allFiles.isNullOrEmpty()) {

                for (file in allFiles) {

                    if (Uri.fromFile(file).toString().endsWith(".mp4")
                    ) {
                        whatsAppModel = WhatsAppModel(
                            name = "WhatsStatus: " + System.currentTimeMillis().toString(),
                            uri = Uri.fromFile(file),
                            path = file.absolutePath,
                            fileName = file.name
                        )
                        tempFilesList.add(whatsAppModel)
                        Log.e("Whatsapp data", tempFilesList.toString())
                    }
                }
            }

            if (!allFilesBusiness.isNullOrEmpty()) {

                for (file in allFilesBusiness) {

                    if (Uri.fromFile(file).toString().endsWith(".mp4")
                    ) {
                        whatsAppModel = WhatsAppModel(
                            name = "WhatsStatusBusiness: " + System.currentTimeMillis().toString(),
                            uri = Uri.fromFile(file),
                            path = file.absolutePath,
                            fileName = file.name
                        )
                        tempFilesList.add(whatsAppModel)
                        Log.e("Whatsapp data", tempFilesList.toString())
                    }
                }
            }
//        Log.e("Whatsapp data", tempFilesList.toString())
        }
        _videosData.value = tempFilesList
    }
}