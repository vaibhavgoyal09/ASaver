package com.mystikcoder.statussaver.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.SavedItemsLayoutBinding
import com.mystikcoder.statussaver.model.SavedPhotos
import com.mystikcoder.statussaver.ui.activity.FullImageViewActivity
import com.mystikcoder.statussaver.ui.activity.VideoPlayActivity

class FileListAdapter(
    private val context: Context,
    private val files: List<SavedPhotos>?,
) : RecyclerView.Adapter<FileListAdapter.FilesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesViewHolder {

        val layoutInflater = LayoutInflater.from(context)

        return FilesViewHolder(
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.saved_items_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FilesViewHolder, position: Int) {
        val fileItem: SavedPhotos = files?.get(position)!!

        holder.binding.imagePlayButton.visibility = if (fileItem.mimeType.contains("image")) View.GONE else View.VISIBLE

        Glide.with(context).load(fileItem.contentUri).placeholder(R.drawable.placeholder_2png)
            .into(holder.binding.imageView)

        holder.binding.imageView.setOnClickListener {
            if (fileItem.mimeType.contains("image")){
                Intent(context, FullImageViewActivity::class.java).also {
                    it.putExtra("imageUri", fileItem.contentUri)
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                }
            }else{
                Intent(context, VideoPlayActivity::class.java).also {
                    it.putExtra("videoUri", fileItem.contentUri)
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                }
            }
        }
    }

    override fun getItemCount() = files?.size ?: 0

    class FilesViewHolder(val binding: SavedItemsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

}