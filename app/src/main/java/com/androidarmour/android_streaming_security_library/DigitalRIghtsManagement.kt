package com.androidarmour.android_streaming_security_library

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager
import com.google.android.exoplayer2.drm.FrameworkMediaDrm
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

class DigitalRIghtsManagement : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

private fun initExoPlayer(context: Context, drmLicenseUrl: String, videoUrl: String) {
    val httpDataSourceFactory = DefaultHttpDataSource.Factory()

    // Set up DRM configuration for Widevine
    val drmLicenseDataSourceFactory = HttpMediaDrmCallback(drmLicenseUrl, httpDataSourceFactory)

    val drmSessionManager = DefaultDrmSessionManager.Builder()
        .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
        .setMultiSession(false)
        .build(drmLicenseDataSourceFactory)

    // Prepare the media source with DRM
    val mediaSource = ProgressiveMediaSource.Factory(httpDataSourceFactory)
        .createMediaSource(MediaItem.fromUri(videoUrl))

    val exoPlayer = ExoPlayer.Builder(context).build().apply {
        setMediaSource(mediaSource)
        prepare()
        playWhenReady = true
    }

    // Attach player to a view for playback
    // playerView.player = exoPlayer
}
