package com.example.mymusic

import android.content.Context
import android.media.MediaPlayer
import android.widget.Toast
import com.example.mymusic.model.Song

//https://medium.com/rocket-fuel/kotlin-by-class-delegation-favor-composition-over-inheritance-a1b97fecd839
class MyPlayer(context: Context, private val player: MediaPlayer) {
    enum class LoopMode {
        NONE, ONCE, ALL, THIS_ONE
    }

    private val lock = Any()

    var loopMode = LoopMode.NONE

    private val playlist = mutableListOf<Song>()

    var currentIndex = -1
        private set

    init {
        player.setOnCompletionListener {
            when(loopMode){
                LoopMode.NONE -> {
                    if(currentIndex < playlist.size - 1){
                        next(context)
                    }
                }
                LoopMode.ONCE -> {
                    player.start()
                    loopMode = LoopMode.NONE
                }
                LoopMode.ALL -> next(context)
                LoopMode.THIS_ONE -> player.start()
            }
        }
        player.setOnErrorListener { _, _, _ ->
            Toast.makeText(context, "Media player error!", Toast.LENGTH_SHORT).show()
            true
        }
    }

    fun loadPlayList(songs: List<Song>) = synchronized(lock) {
        playlist.clear()
        playlist.addAll(songs)
        player.reset()
    }

    fun playAt(context: Context, index: Int) = synchronized(lock) {
        if (index in playlist.indices) {
            val song = playlist[index]
            player.setDataSource(context, song.mediaUri)
            player.prepare()
            player.start()
        }
    }

    fun next(context: Context) = synchronized(lock) {
        val nextIndex = (currentIndex + 1) % playlist.size
        check(nextIndex in playlist.indices)
        val song = playlist[nextIndex]
        player.setDataSource(context, song.mediaUri)
    }

    fun previous(context: Context) = synchronized(lock) {
        player.reset()
        val previousIndex = (currentIndex - 1 + playlist.size) % playlist.size
        check(previousIndex in playlist.indices)
        val song = playlist[previousIndex]
        player.setDataSource(context, song.mediaUri)
        player.prepare()
        player.start()
    }

    fun play(){
        player.prepare()
        player.start()
    }

    fun pause(){
        player.pause()
    }

    fun stop() {
        player.stop()
    }

    fun release(){
        player.release()
    }

    fun peek(msec: Int){
        player.seekTo(msec)
    }
}
