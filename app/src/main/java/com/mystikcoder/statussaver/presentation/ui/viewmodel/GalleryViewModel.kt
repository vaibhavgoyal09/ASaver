package com.mystikcoder.statussaver.presentation.ui.viewmodel

import android.app.Application
import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mystikcoder.statussaver.domain.model.util.SavedPhotos
import com.mystikcoder.statussaver.presentation.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val app: Application
) : AndroidViewModel(app) {

    private val _savedFiles = MutableLiveData<List<SavedPhotos>>()
    val savedFiles: LiveData<List<SavedPhotos>> = _savedFiles
    private val tempListFiles = mutableListOf<SavedPhotos>()

    private val _hasPermission = MutableLiveData<Boolean>()
    val hasPermissions : LiveData<Boolean> = _hasPermission

    init {

        val permissionsGranted = Utils.hasReadPermission(app)
        _hasPermission.value = permissionsGranted
        if (permissionsGranted) {
            viewModelScope.launch {
                getSavedFiles("All")
            }
        }
    }

    fun permissionsGranted() {
        _hasPermission.postValue(true)
    }

    suspend fun getSavedFiles(name: String) {

        if (tempListFiles.isNotEmpty()) {
            tempListFiles.clear()
        }

        withContext(Dispatchers.IO) {

            if (Build.VERSION.SDK_INT >= 29) {

                val collection = MediaStore.Downloads.INTERNAL_CONTENT_URI

                val projection = arrayOf(
                    MediaStore.Downloads._ID,
                    MediaStore.Downloads.MIME_TYPE
                )

                try {
                    app.contentResolver.query(
                        collection,
                        projection,
                        null,
                        null,
                        "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
                    )?.use { cursor ->
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                        val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads.MIME_TYPE)

                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idColumn)
                            val mimeType = cursor.getString(mimeTypeColumn)
                            val contentUri = ContentUris.withAppendedId(
                                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                id
                            )
                            tempListFiles.add(SavedPhotos(contentUri.toString(), mimeType))
                            Timber.e(contentUri.toString())
                        }
                        withContext(Dispatchers.Main){
                            _savedFiles.value = tempListFiles.toList()
                        }
                    }
                }catch (e: Exception){
                    Timber.e(e.message ?: "")
                    withContext(Dispatchers.Main){
                        _savedFiles.value = tempListFiles.toList()
                    }
                }

            } else {
                val path: String = when (name) {
                    "All" -> Utils.PATH_ROOT_DIRECTORY_APP
                    "Instagram" -> Utils.PATH_ROOT_DIRECTORY_INSTAGRAM.toString()
                    "Josh" -> Utils.PATH_ROOT_DIRECTORY_JOSH.toString()
                    "Chingari" -> Utils.PATH_ROOT_DIRECTORY_CHINGARI.toString()
                    "ShareChat" -> Utils.PATH_ROOT_DIRECTORY_SHARE_CHAT.toString()
                    "Mitron" -> Utils.PATH_ROOT_DIRECTORY_MITRON.toString()
                    "MxTakaTak" -> Utils.PATH_ROOT_DIRECTORY_MX_TAKA_TAK.toString()
                    "WhatsApp" -> Utils.PATH_ROOT_DIRECTORY_WHATSAPP.toString()
                    "Moj" -> Utils.PATH_ROOT_DIRECTORY_MOJ.toString()
                    "Roposo" -> Utils.PATH_ROOT_DIRECTORY_ROPOSSO.toString()
                    "Twitter" -> Utils.PATH_ROOT_DIRECTORY_TWITTER.toString()
                    "TikTok" -> Utils.PATH_ROOT_DIRECTORY_TIK_TOK.toString()
                    "Likee" -> Utils.PATH_ROOT_DIRECTORY_LIKEE.toString()
                    else -> null
                } ?: return@withContext

                val directory = File(path)
                val files = directory.listFiles()
                addFileToList(files)
            }
        }
    }

    private suspend fun addFileToList(files: Array<out File>?) {
        if (!files.isNullOrEmpty()) {
            for (file in files) {
                if (file.isDirectory) {
                    addFileToList(file.listFiles())
                } else {
                    val contentUri = file.path
                    val mimeType = if (file.path.endsWith(".mp4")) "video" else "image"
                    tempListFiles.add(SavedPhotos(contentUri, mimeType))
                    Timber.e(mimeType)
                }
            }
            withContext(Dispatchers.Main){
                _savedFiles.value = tempListFiles.toList()
            }
        } else {
            withContext(Dispatchers.Main){
                _savedFiles.value = listOf()
            }
        }
    }
}
