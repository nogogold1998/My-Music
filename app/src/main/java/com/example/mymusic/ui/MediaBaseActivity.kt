package com.example.mymusic.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.example.mymusic.data.model.LocalAudio
import com.example.mymusic.receiver.MediaControlReceiver
import com.example.mymusic.service.MediaService
import com.example.mymusic.util.MediaManager

abstract class MediaBaseActivity : AppCompatActivity(), MediaManager.Listener{
    private var receiver: MediaControlReceiver? = null
    protected var mediaService: MediaService? = null

    protected var mediaServiceBound: Boolean = false
    protected var isPlay: Boolean = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as? MediaService.MediaBinder
            mediaService = binder?.getMediaService()
            mediaService?.nowPlaying()?.let {
                changeNowPlaying(it)
                isPlay = true
            }
            mediaServiceBound = mediaService != null
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mediaService = null
            mediaServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = MediaControlReceiver(this)
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MediaService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        val filter = IntentFilter().apply {
            addAction(MediaService.ACTION_ON_TICK)
            addAction(MediaService.ACTION_PLAYBACK_PLAY)
            addAction(MediaService.ACTION_PLAYBACK_PAUSE)
            addAction(MediaService.ACTION_SONG_CHANGE)
        }
        registerReceiver(receiver, filter)
    }

    override fun onStop() {
        super.onStop()
        if (isPlay) {
            startService(Intent(this, MediaService::class.java))
        }
        if (mediaServiceBound) {
            // ContextCompat.startForegroundService(this, Intent(this, MediaService::class.java))
            unbindService(serviceConnection)
            mediaServiceBound = false
        }
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        receiver = null
    }

    override fun onPlayPause(isPlay: Boolean) {
        this.isPlay = isPlay
    }

    protected abstract fun changeNowPlaying(localAudio: LocalAudio)
}
