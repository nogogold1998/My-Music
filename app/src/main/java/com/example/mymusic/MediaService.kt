package com.example.mymusic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.mymusic.repo.model.LocalAudio
import com.example.mymusic.ui.MediaActivity

class MediaService : Service() {
    inner class MediaBinder : Binder() {
        fun getMediaService(): MediaService = this@MediaService
    }

    private val binder = MediaBinder()

    private var mediaManager: MediaManager? = null

    private lateinit var listener: MediaManager.Listener

    override fun onBind(intent: Intent): IBinder {
        // mediaPlayer.setDataSource()
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        listener = object : MediaManager.Listener {

            override fun onTick(currentPosition: Int) {
                val intent = Intent().apply {
                    action = ACTION_ON_TICK
                    putExtra(EXTRA_SONG_CURRENT_POSITION, currentPosition)
                }
                sendBroadcast(intent)
            }

            override fun onSongChanged(songId: Long) {
                val intent = Intent().apply {
                    action = ACTION_SONG_CHANGE
                    putExtra(EXTRA_SONG_ID, songId)
                }
                sendBroadcast(intent)
            }

            override fun onPlayPause(isPlay: Boolean) {
                val song = checkNotNull(mediaManager?.currentIndexLocalAudio).second
                val intent = Intent().apply {
                    action = if (isPlay) ACTION_PLAYBACK_PLAY else ACTION_PLAYBACK_PAUSE
                }
                sendBroadcast(intent)

                val notification = buildNotification(song, isPlay)
                if (isPlay) {
                    startForeground(NOTIFICATION_PLAYBACK_ID, notification)
                } else {
                    ContextCompat.getSystemService(baseContext, NotificationManager::class.java)
                        ?.run {
                            notify(NOTIFICATION_PLAYBACK_ID, notification)
                        }
                    stopForeground(false)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                ACTION_PLAYBACK_PLAY -> play()
                ACTION_PLAYBACK_PAUSE -> pause()
                ACTION_PLAYBACK_NEXT -> next()
                ACTION_PLAYBACK_PREVIOUS -> previous()
                ACTION_PLAYBACK_STOP -> stop()
                else -> {
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaManager?.release()
        mediaManager = null
    }

    fun next() {
        mediaManager?.next()
    }

    fun previous() {
        mediaManager?.previous()
    }

    fun play() {
        mediaManager?.play()
    }

    fun pause() {
        mediaManager?.pause()
    }

    private fun stop() {
        mediaManager?.pause()
        ContextCompat.getSystemService(this, NotificationManager::class.java)
            ?.cancel(NOTIFICATION_PLAYBACK_ID)
        stopSelf()
    }

    fun playSongWithId(id: Long) {
        mediaManager?.playWithId(id)
    }

    fun seekTo(msec: Int) {
        mediaManager?.seekTo(msec)
        mediaManager?.play()
    }

    fun loadPlaylist(list: List<LocalAudio>) {
        mediaManager?.unsubscribe(listener)
        mediaManager?.release()
        mediaManager = MediaManager(this, MediaPlayer(), list)
        mediaManager!!.subscribe(listener)
    }

    fun nowPlaying(): LocalAudio? =  mediaManager?.currentIndexLocalAudio?.second

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.getSystemService(this, NotificationManager::class.java)?.let {
                val channelId = getString(R.string.media_playback_channel_id)
                if (it.getNotificationChannel(channelId) == null) {
                    val channel = NotificationChannel(
                        channelId,
                        getString(R.string.media_playback_channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply { description = getString(R.string.media_playback_channel_description) }
                    it.createNotificationChannel(channel)
                }
            }
        }
    }

    private fun buildNotification(localAudio: LocalAudio, isPlay: Boolean): Notification {
        val builder = NotificationCompat.Builder(
            baseContext,
            getString(R.string.media_playback_channel_id)
        ).apply {
            setContentTitle(localAudio.title)
            setContentText(localAudio.artist)
            setSubText(localAudio.album)
            setLargeIcon(localAudio.coverImage)

            run {
                val intent = Intent(baseContext, MediaActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                val pendingIntent = PendingIntent.getActivity(
                    baseContext,
                    REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT
                )
                setContentIntent(pendingIntent)
            }
            setDeleteIntent(buildMediaServicePendingIntent(ACTION_PLAYBACK_STOP))
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setSmallIcon(R.drawable.ic_launcher_foreground)
            color = ContextCompat.getColor(
                baseContext,
                R.color.colorPrimaryDark
            )


            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_round_skip_previous,
                    getString(R.string.skip_previous),
                    buildMediaServicePendingIntent(ACTION_PLAYBACK_PREVIOUS)
                )
            )
            if (isPlay) {
                addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_round_pause,
                        getString(R.string.pause),
                        buildMediaServicePendingIntent(ACTION_PLAYBACK_PAUSE)
                    )
                )
            } else {
                addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_round_play,
                        getString(R.string.play),
                        buildMediaServicePendingIntent(ACTION_PLAYBACK_PLAY)
                    )
                )
            }
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_round_skip_next,
                    getString(R.string.skip_next),
                    buildMediaServicePendingIntent(ACTION_PLAYBACK_NEXT)
                )
            )
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(1)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(buildMediaServicePendingIntent(ACTION_PLAYBACK_STOP))
            )
        }
        return builder.build()
    }

    private fun buildMediaServicePendingIntent(action: String) =
        Intent(baseContext, MediaService::class.java).let {
            it.action = action
            PendingIntent.getService(
                baseContext,
                REQUEST_CODE,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

    companion object {
        const val TAG = "MediaService"

        const val REQUEST_CODE = 10

        const val NOTIFICATION_PLAYBACK_ID = 16

        const val ACTION_PLAYBACK_PLAY = "action.playback.play"
        const val ACTION_PLAYBACK_PAUSE = "action.playback.pause"
        const val ACTION_PLAYBACK_STOP = "action.playback.stop"
        const val ACTION_PLAYBACK_NEXT = "action.playback.next"
        const val ACTION_PLAYBACK_PREVIOUS = "action.playback.previous"

        const val ACTION_SONG_CHANGE = "action.song.change"
        const val ACTION_ON_TICK = "action.on.tick"

        const val EXTRA_SONG_CURRENT_POSITION = "extra.song.current.position"
        const val EXTRA_SONG_ID = "extra.song.current.id"
    }
}
