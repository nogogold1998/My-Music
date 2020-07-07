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
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.mymusic.model.Song

class MediaService : Service() {
    private var myPlayerNotification: Notification? = null

    private val binder = MediaBinder()

    private val mediaPlayer: MediaPlayer = MediaPlayer()

    private val songList = mutableListOf<Song>()

    override fun onBind(intent: Intent): IBinder {
        // mediaPlayer.setDataSource()
        return binder
    }

    inner class MediaBinder : Binder() {
        fun getMediaService(): MediaService = this@MediaService
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        mediaPlayer.setOnErrorListener { mp, what, extra ->
            Toast.makeText(baseContext, "Media player failed", Toast.LENGTH_SHORT).show()
            true
        }
        mediaPlayer.setOnCompletionListener {

        }
        mediaPlayer.setOnPreparedListener {

        }
    }

    fun loadMedia(songs: List<Song>) {
        songList.clear()
        songList.addAll(songs)
    }

    fun playAtPosition(position: Int) {
        if (position in songList.indices) {
            mediaPlayer.setDataSource(baseContext, songList[position].mediaUri)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.getSystemService(this, NotificationManager::class.java)?.let {
                val channelId = getString(R.string.media_playback_channel_id)
                if (it.getNotificationChannel(channelId) == null) {
                    val channel = NotificationChannel(
                        channelId,
                        getString(R.string.media_playback_channel_name),
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply { description = getString(R.string.media_playback_channel_description) }
                    it.createNotificationChannel(channel)
                }
            }
        }
    }

    private fun buildNotification(song: Song): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(
            baseContext,
            getString(R.string.media_playback_channel_id)
        ).apply {
            setContentTitle(song.title)
            setContentText(song.artist)
            setSubText(song.album)
            setLargeIcon(song.coverImage)

            run {
                val intent = Intent(baseContext, MediaService::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
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

            setStyle(
                MediaStyle()
                    .setShowActionsInCompactView(1)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(buildMediaServicePendingIntent(ACTION_PLAYBACK_STOP))
            )

            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_round_skip_previous,
                    getString(R.string.skip_previous),
                    buildMediaServicePendingIntent(ACTION_PLAYBACK_PREVIOUS)
                )
            )
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_round_skip_pause,
                    getString(R.string.pause),
                    buildMediaServicePendingIntent(ACTION_PLAYBACK_PAUSE)
                )
            )
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_round_skip_next,
                    getString(R.string.skip_next),
                    buildMediaServicePendingIntent(ACTION_PLAYBACK_NEXT)
                )
            )
        }
        return builder
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

        const val ACTION_PLAYBACK_START = "action.playback.start"
        const val ACTION_PLAYBACK_PAUSE = "action.playback.pause"
        const val ACTION_PLAYBACK_STOP = "action.playback.stop"
        const val ACTION_PLAYBACK_NEXT = "action.playback.next"
        const val ACTION_PLAYBACK_PREVIOUS = "action.playback.previous"
        const val ACTION_PLAYBACK_SEEK = "action.playback.seek"
    }
}
