package com.mystikcoder.statussaver.framework.presentation.ui.viewmodel

import android.app.Application
import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mystikcoder.statussaver.core.domain.model.util.SavedPhotos
import com.mystikcoder.statussaver.framework.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
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
            CoroutineScope(Dispatchers.IO).launch {
                getSavedFiles("All")
            }
        }else{
            Timber.e("No Permissions")
        }
    }

    fun permissionsGranted() {
        _hasPermission.postValue(true)
    }

    fun getSavedFiles(name: String) {

        if (tempListFiles.isNotEmpty()) {
            tempListFiles.clear()
        }

        viewModelScope.launch(Dispatchers.IO) {

            if (Build.VERSION.SDK_INT >= 29) {

                val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI

                val projection = arrayOf(
                    MediaStore.Downloads._ID,
                    MediaStore.Downloads.MIME_TYPE,
                    MediaStore.Downloads.RELATIVE_PATH
                )

                val selectionArguments = when(name) {
                    "Instagram" -> arrayOf("%ASaver/Instagram%")
                    "Twitter" -> arrayOf("%ASaver/Twitter%")
                    "Moj" -> arrayOf("%ASaver/Moj%")
                    "Mitron" -> arrayOf("%ASaver/Mitron%")
                    "ShareChat" -> arrayOf("%ASaver/ShareChat%")
                    "MxTakaTak" -> arrayOf("%ASaver/MxTakaTak%")
                    "Chingari" -> arrayOf("%ASaver/Chingari%")
                    "Josh" -> arrayOf("%ASaver/Josh%")
                    "Likee" -> arrayOf("%ASaver/Likee%")
                    "Roposo" -> arrayOf("%ASaver/Roposo%")
                    "TikTok" -> arrayOf("%ASaver/TikTok%")
                    else -> arrayOf("%ASaver%")
                }

                try {
                    app.contentResolver.query(
                        collection,
                        projection,
                        "${MediaStore.Downloads.RELATIVE_PATH} LIKE ? ",
                        selectionArguments,
                        "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
                    )?.use { cursor ->
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                        val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads.MIME_TYPE)
                        val relativePathColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads.RELATIVE_PATH)

                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idColumn)
                            val mimeType = cursor.getString(mimeTypeColumn)
                            val contentUri = ContentUris.withAppendedId(
                                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                id
                            )
                            val relativePath = cursor.getString(relativePathColumn)
                            tempListFiles.add(SavedPhotos(contentUri.toString(), mimeType))
                            Timber.e(contentUri.toString())
                            Timber.e(relativePath)
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
                    "All" -> Utils.DIRECTORY_ASAVER.absolutePath
                    "Instagram" -> Utils.DIRECTORY_INSTAGRAM_FILE.toString()
                    "Josh" -> Utils.DIRECTORY_JOSH_FILE.toString()
                    "Chingari" -> Utils.DIRECTORY_CHINGARI_FILE.toString()
                    "ShareChat" -> Utils.DIRECTORY_SHARECHAT_FILE.toString()
                    "Mitron" -> Utils.DIRECTORY_MITRON_FILE.toString()
                    "MxTakaTak" -> Utils.DIRECTORY_MX_TAKA_TAK_FILE.toString()
                    "Moj" -> Utils.DIRECTORY_MOJ_FILE.toString()
                    "Roposo" -> Utils.DIRECTORY_ROPOSSO_FILE.toString()
                    "Twitter" -> Utils.DIRECTORY_TWITTER_FILE.toString()
                    "TikTok" -> Utils.DIRECTORY_TIK_TOK_FILE.toString()
                    "Likee" -> Utils.DIRECTORY_LIKEE_FILE.toString()
                    else -> null
                } ?: return@launch

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
                _savedFiles.value = emptyList()
            }
        }
    }
}
