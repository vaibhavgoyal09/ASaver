package com.mystikcoder.statussaver.presentation.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.LayoutVideosItemsBinding
import com.mystikcoder.statussaver.domain.model.facebook.FacebookNode
import com.mystikcoder.statussaver.domain.model.instagram.ItemModel
import com.mystikcoder.statussaver.extensions.showShortToast
import com.mystikcoder.statussaver.presentation.ui.activity.FullImageViewActivity
import com.mystikcoder.statussaver.presentation.ui.activity.VideoPlayActivity
import com.mystikcoder.statussaver.presentation.utils.NetworkState
import com.mystikcoder.statussaver.presentation.utils.Utils

class StoryItemsAdapter(
    private val context: Context,
    private val instaStories: List<ItemModel>?,
    private val itemsToBind: String,
    private val facebookStories: List<FacebookNode>?
) :
    RecyclerView.Adapter<StoryItemsAdapter.StoriesViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StoryItemsAdapter.StoriesViewHolder {
        val layoutInflater = LayoutInflater.from(context)

        return StoriesViewHolder(
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.layout_videos_items,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StoryItemsAdapter.StoriesViewHolder, position: Int) {
        if (itemsToBind == "Instagram") {
            holder.bindInstagramData(instaStories!![position])
        } else {
            holder.bindFacebookData(facebookStories!![position])
        }
    }

    override fun getItemCount(): Int {
        return if (itemsToBind == "Instagram") {
            if (instaStories?.size == 0) {
                0
            } else {
                instaStories!!.size
            }
        } else {
            if (facebookStories?.size == 0) {
                0
            } else {
                facebookStories!!.size
            }
        }
    }

    inner class StoriesViewHolder(private val binding: LayoutVideosItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindInstagramData(story: ItemModel) {
            binding.imagePlayButton.visibility =
                if (story.mediaType == 2) View.VISIBLE else View.GONE

            Glide.with(context).load(story.imageVersions2.candidates[0].url)
                .placeholder(R.drawable.placeholder_2png).into(binding.imageView)

            binding.imageView.setOnClickListener {
                if (story.mediaType == 2) {
                    Intent(context, VideoPlayActivity::class.java).also {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        it.putExtra("videoUri", story.videoVersions[0].url)
                        context.startActivity(it)
                    }
                } else {
                    Intent(context, FullImageViewActivity::class.java).also {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        it.putExtra("imageUri", story.imageVersions2.candidates[0].url)
                        context.startActivity(it)
                    }
                }
            }

            binding.buttonDownload.setOnClickListener {
                if (NetworkState.isNetworkAvailable()) {
                    if (Utils.hasWritePermission(context)) {
                        if (story.mediaType == 2) {
                            Utils.startDownload(
                                story.videoVersions[0].url,
                                Utils.DIRECTORY_INSTAGRAM,
                                context,
                                "story_ ${story.id}.mp4"
                            )
                        } else {
                            Utils.startDownload(
                                story.imageVersions2.candidates[0].url,
                                Utils.DIRECTORY_INSTAGRAM,
                                context,
                                "story_ ${story.id}.png"
                            )
                        }
                    } else {
                        context.showShortToast("Require storage permission")
                    }
                } else {
                    context.showShortToast("No Internet connection available")
                }
            }
        }

        fun bindFacebookData(nodeModel: FacebookNode) {
            binding.imagePlayButton.visibility =
                if (nodeModel.nodeData.attachmentsList[0].mediaData.typeName.equals(
                        "Video",
                        true
                    )
                ) View.VISIBLE
                else View.GONE

            Glide.with(context)
                .load(nodeModel.nodeData.attachmentsList[0].mediaData.previewImage.get("uri").asString)
                .placeholder(R.drawable.placeholder_2png)
                .thumbnail(0.2f).into(binding.imageView)

            try {
                binding.imageView.setOnClickListener {

                    if (nodeModel.nodeData.attachmentsList[0].mediaData.typeName.equals(
                            "Video",
                            true
                        )
                    ) {
                        Intent(context, VideoPlayActivity::class.java).also {
                            it.putExtra(
                                "videoUri",
                                nodeModel.nodeData.attachmentsList[0].mediaData.playableUrlQualityHd
                            )
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(it)
                        }
                    } else {
                        Intent(context, FullImageViewActivity::class.java).also {
                            it.putExtra(
                                "imageUri",
                                nodeModel.nodeData.attachmentsList[0].mediaData.previewImage.get("uri").asString
                            )
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(it)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            binding.buttonDownload.setOnClickListener {
                if (NetworkState.isNetworkAvailable()) {
                    if (Utils.hasWritePermission(context)) {
                        if (nodeModel.nodeData.attachmentsList[0].mediaData.typeName.equals(
                                "Video",
                                true
                            )
                        ) {

                            Utils.startDownload(
                                nodeModel.nodeData.attachmentsList[0].mediaData.playableUrlQualityHd,
                                Utils.DIRECTORY_INSTAGRAM,
                                context,
                                "FbStory" + System.currentTimeMillis().toString() + ".mp4"
                            )
                        } else {
                            Utils.startDownload(
                                nodeModel.nodeData.attachmentsList[0].mediaData.previewImage.get("uri").asString,
                                Utils.DIRECTORY_INSTAGRAM,
                                context,
                                "FbStory" + System.currentTimeMillis().toString() + ".png"
                            )
                        }
                    } else {
                        context.showShortToast("Require storage permission")
                    }
                } else {
                    context.showShortToast("No internet connection available")
                }
            }
        }
    }
}
