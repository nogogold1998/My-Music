package com.example.mymusic

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder

class MediaService : Service() {
    private val binder = object : MediaBinder() {
        override fun getMediaService(): MediaService = this@MediaService
    }

    private val mediaPlayer: MediaPlayer = MediaPlayer()

    override fun onBind(intent: Intent): IBinder {
        // mediaPlayer.setDataSource()
        return binder
    }

    abstract class MediaBinder : Binder() {
        abstract fun getMediaService(): MediaService
    }
}
