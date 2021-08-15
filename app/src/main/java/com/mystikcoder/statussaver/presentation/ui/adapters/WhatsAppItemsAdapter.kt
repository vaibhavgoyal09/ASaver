package com.mystikcoder.statussaver.presentation.ui.adapters

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.WhatsappItemsLayoutBinding
import com.mystikcoder.statussaver.domain.model.whatsapp.WhatsAppModel
import com.mystikcoder.statussaver.extensions.showShortToast
import com.mystikcoder.statussaver.presentation.ui.activity.FullImageViewActivity
import com.mystikcoder.statussaver.presentation.ui.activity.VideoPlayActivity
import com.mystikcoder.statussaver.presentation.utils.Utils
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

class WhatsAppItemsAdapter(
    val context: Context,
    private val files: ArrayList<WhatsAppModel>?
) :
    RecyclerView.Adapter<WhatsAppItemsAdapter.ItemsViewHolder>() {

    val savedFilePath: String = Utils.PATH_ROOT_DIRECTORY_WHATSAPP.toString() + "/"

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WhatsAppItemsAdapter.ItemsViewHolder {
        val layoutInflater = LayoutInflater.from(context)

        return ItemsViewHolder(
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.whatsapp_items_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: WhatsAppItemsAdapter.ItemsViewHolder, position: Int) {
        holder.bindItems(files?.get(position)!!)
    }

    override fun getItemCount() = files?.size ?: 0

    inner class ItemsViewHolder(private val binding: WhatsappItemsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindItems(file: WhatsAppModel) {

            binding.imageView.setOnClickListener {
                if (file.uri.toString().endsWith(".mp4")) {
                    Intent(context, VideoPlayActivity::class.java).also {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        it.putExtra("videoUri", file.path)
                        context.startActivity(it)
                    }
                } else {
                    Intent(context, FullImageViewActivity::class.java).also {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        it.putExtra("imageUri", file.path)
                        context.startActivity(it)
                    }
                }
            }

            binding.imagePlayButton.visibility =
                if (file.uri.toString().endsWith(".mp4")) View.VISIBLE else View.GONE

            Glide.with(context).load(file.path).placeholder(R.drawable.placeholder_2png).into(binding.imageView)

            binding.buttonDownload.setOnClickListener {
                if (Utils.hasWritePermission(context)){
                    val filename = (file.path).substring((file.path).lastIndexOf("/") + 1)
                    val tempFile = File(file.path)
                    val destFile = File(savedFilePath)

                    try {
                        FileUtils.copyFileToDirectory(tempFile, destFile)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val changedFileName = filename.substring(12)
                    val newFile = File(savedFilePath + changedFileName)
                    val contentType: String =
                        if (file.uri.toString().endsWith(".mp4")) "video/*" else "image/*"

                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(newFile.absolutePath),
                        arrayOf(contentType),
                        object : MediaScannerConnection.MediaScannerConnectionClient {
                            override fun onScanCompleted(path: String?, uri: Uri?) {
                            }

                            override fun onMediaScannerConnected() {
                            }
                        }
                    )
                    val fileFrom = File(savedFilePath, filename)
                    val fileTo = File(savedFilePath, changedFileName)

                    fileFrom.renameTo(fileTo)
                    Toast.makeText(
                        context,
                        "File saved in $savedFilePath $changedFileName",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }else{
                    context.showShortToast("Require storage permission")
                }
            }
        }
    }
}