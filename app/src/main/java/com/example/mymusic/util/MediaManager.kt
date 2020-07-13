package com.example.mymusic.util

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.util.Log
import com.example.mymusic.data.model.LocalAudio

class MediaManager(
    private val context: Context,
    private val player: MediaPlayer,
    private val playlist: List<LocalAudio>
) {
    interface Listener {
        fun onTick(currentPosition: Int)

        fun onSongChanged(songId: Long)

        fun onPlayPause(isPlay: Boolean)
    }

    enum class LoopMode {
        NONE, ONCE, ALL, THIS_ONE
    }

    companion object {
        private const val TAG = "MediaManager"

        private const val COUNTER_DELAY_MILLIS = 500L
    }

    var loopMode = LoopMode.NONE

    var currentIndexLocalAudio: Pair<Int, LocalAudio>? = null
        private set

    private val lock = Any()

    private val listeners = mutableListOf<Listener>()

    private val handler = Handler()

    private val counter = object : Runnable {
        override fun run() {
            notifyTimeTick()
            handler.postDelayed(this,
                COUNTER_DELAY_MILLIS
            )
        }
    }

    init {
        player.setOnCompletionListener {
            when (loopMode) {
                LoopMode.NONE -> next()
                LoopMode.ONCE -> {
                    player.start()
                    loopMode =
                        LoopMode.NONE
                }
                LoopMode.ALL -> next()
                LoopMode.THIS_ONE -> player.start()
            }
        }
        player.setOnErrorListener { _, _, _ ->
            Log.e(TAG, "MediaPlayer failed ")
            true
        }
    }

    fun playWithId(id: Long) = synchronized(lock) {
        playlist.indexOfFirst { it.id == id }
            .takeIf { it in playlist.indices }
            ?.let { index ->
                val song = playlist[index]
                currentIndexLocalAudio = index to song
                changeSong(song)
            }
    }

    fun next() = synchronized(lock) {
        if (currentIndexLocalAudio != null && playlist.isNotEmpty()) {
            val nextIndex = ((currentIndexLocalAudio?.first ?: -1) + 1) % playlist.size
            val song = playlist[nextIndex]
            currentIndexLocalAudio = nextIndex to song
            changeSong(song)
        }
    }

    fun previous() = synchronized(lock) {
        if (currentIndexLocalAudio != null && playlist.isNotEmpty()) {
            val previousIndex = ((currentIndexLocalAudio?.first ?: 1) - 1 + playlist.size) % playlist.size
            check(previousIndex in playlist.indices)
            val song = playlist[previousIndex]
            currentIndexLocalAudio = previousIndex to song
            changeSong(song)
        }
    }

    fun play() {
        if (currentIndexLocalAudio != null) {
            player.start()
            notifyPausePlay(true)
            handler.post(counter)
        }
    }

    fun pause() {
        player.pause()
        notifyPausePlay(false)
    }

    fun stop() {
        player.stop()
        notifyPausePlay(false)
        handler.removeCallbacks(counter)
    }

    fun release() {
        player.release()
        currentIndexLocalAudio = null
        listeners.clear()
        handler.removeCallbacks(counter)
    }

    fun seekTo(msec: Int) {
        player.seekTo(msec)
    }

    fun subscribe(listener: Listener) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: Listener) {
        listeners.remove(listener)
    }

    private fun changeSong(localAudio: LocalAudio){
        player.stop()
        player.reset()
        player.setDataSource(context, localAudio.mediaUri)
        player.prepare()
        play()
        if (currentIndexLocalAudio != null) {
            listeners.forEach { it.onSongChanged(currentIndexLocalAudio!!.second.id) }
        }
    }

    private fun notifyTimeTick() {
        listeners.forEach { it.onTick(player.currentPosition) }
    }

    private fun notifyPausePlay(isPlay: Boolean) {
        listeners.forEach { it.onPlayPause(isPlay) }
    }
}
