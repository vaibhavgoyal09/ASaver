package com.mystikcoder.statussaver.presentation.ui.activity

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.MimeTypes
import com.mystikcoder.statussaver.R
import timber.log.Timber

class VideoPlayActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private var exoPlayer: SimpleExoPlayer? = null
    private var isPlayWhenReady: Boolean = true
    private var currWindow: Int = 0
    private var playbackPosition: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_video_play)

        hideSystemUi()
        playerView = findViewById(R.id.playerView)
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < 24 && exoPlayer == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        if (exoPlayer == null) {
            val trackSelector = DefaultTrackSelector(this)
            trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd())

            exoPlayer = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()

            playerView.player = exoPlayer

            Timber.e(intent.getStringExtra("videoUri"))

            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(intent.getStringExtra("videoUri")))
                .setMimeType(MimeTypes.APPLICATION_MP4)
                .build()

            exoPlayer?.let {
                it.setMediaItem(mediaItem)
                it.playWhenReady = isPlayWhenReady
                it.seekTo(currWindow, playbackPosition)
                it.prepare()
            }
        }
    }

    private fun releasePlayer() {
        exoPlayer?.let { player ->
            playbackPosition = player.currentPosition
            isPlayWhenReady = player.playWhenReady
            currWindow = player.currentWindowIndex
            player.release()
            exoPlayer = null
        }
    }

    @Suppress("DEPRECATION")
    private fun hideSystemUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }
}
