package com.example.mymusic.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.mymusic.util.MediaManager
import com.example.mymusic.service.MediaService

class MediaControlReceiver(private val listener: MediaManager.Listener) :
    BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                MediaService.ACTION_SONG_CHANGE ->
                    intent.getLongExtra(MediaService.EXTRA_SONG_ID, -1)
                        .takeIf { it != -1L }
                        ?.let { id -> listener.onSongChanged(id) }

                MediaService.ACTION_PLAYBACK_PAUSE -> listener.onPlayPause(false)
                MediaService.ACTION_PLAYBACK_PLAY -> listener.onPlayPause(true)
                MediaService.ACTION_ON_TICK -> {
                    val position =
                        intent.getIntExtra(MediaService.EXTRA_SONG_CURRENT_POSITION, 0)
                    listener.onTick(position)
                }
                else -> Log.d(
                    "HelperReceiver",
                    "onReceive: unknown action"
                )
            }
        }
    }
}
