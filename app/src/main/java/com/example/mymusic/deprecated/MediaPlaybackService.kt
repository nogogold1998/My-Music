package com.example.mymusic.deprecated

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.example.mymusic.R

private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

@Deprecated("", level = DeprecationLevel.WARNING /*ERROR*/)
class MediaPlaybackService : MediaBrowserServiceCompat() {
    private val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> player.pause()
            AudioManager.AUDIOFOCUS_GAIN -> player.start()
        }
    }
    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver
    private var myPlayerNotification: Notification? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var player: android.media.MediaPlayer
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private lateinit var audioFocusRequest: AudioFocusRequestCompat

    private val am: AudioManager by lazy { baseContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            // request audio focus for playback, this registers the afChangeListener
            audioFocusRequest =
                AudioFocusRequestCompat
                    .Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(afChangeListener)
                    .setAudioAttributes(
                        AudioAttributesCompat
                            .Builder()
                            .setContentType((AudioAttributesCompat.CONTENT_TYPE_MUSIC))
                            .setUsage(AudioAttributesCompat.USAGE_MEDIA)
                            .build()
                    )
                    .build()
            val result = AudioManagerCompat.requestAudioFocus(am, audioFocusRequest)

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // start the service
                startService(Intent(baseContext, MediaBrowserService::class.java))
                // set the session active todo(and up date metadata and state)
                mediaSession.isActive = true
                // start the player
                player.start()
                // register BECOME_NOISY BroadcastReceiver
                registerReceiver(becomingNoisyReceiver, intentFilter)
                // put the service in the foreground, post nofitication
                this@MediaPlaybackService.buildNotification()
            }
            TODO("Build and display the notification when the player starts playing")
            // val controller =
        }

        override fun onStop() {
            // abandon audio focus
            AudioManagerCompat.abandonAudioFocusRequest(am, audioFocusRequest)
            // unregisterReceiver
            unregisterReceiver(becomingNoisyReceiver)
            // stop the service
            this@MediaPlaybackService.stopSelf()
            // set the session inactive todo(and update metadata and state)
            mediaSession.isActive = false
            // stop the player
            player.stop()
            // the the service out of the foreground
            this@MediaPlaybackService.stopForeground(false)
        }

        override fun onPause() {
            // todo update metadata and state
            // pause the player
            player.pause()
            // unregister BECOME_NOISY BroadcastReceiver
            unregisterReceiver(becomingNoisyReceiver)
            // take the service out of the foreground, retain the notification
            this@MediaPlaybackService.stopForeground(false)
        }
    }

    override fun onCreate() {
        super.onCreate()

        // build a PendingIntent that can be used to launch the UI
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        // create a MediaSessionCompat
        mediaSession = MediaSessionCompat(this,
            TAG
        ).apply {
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            this@MediaPlaybackService.stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState(stateBuilder.build())

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(mediaSessionCallback)

            // set the session's token so that client activities can communicate with it
            this@MediaPlaybackService.sessionToken = sessionToken
        }

        player = android.media.MediaPlayer()

        createChannel()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        // (optional) control the level of access for the specified package name
        // you'll need to write your own logic to do this.
        // return if (allowBrowsing(clientPackageName, clientUid)) {
        //     // return a root id that clients can use with onLoadChildren() to retrieve
        //     // the content hierarchy
        //     BrowserRoot(MY_MEDIA_ROOT_ID, null)
        // } else {
        //     // clients can connect, but this BrowserRoot is an empty hierachy
        //     // so onLoadChildren returns nothing. this disables the ability to browse for content
        //     BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
        // }
        return BrowserRoot(MY_MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        // browsing not allowed
        if (MY_EMPTY_MEDIA_ROOT_ID == parentId) {
            result.sendResult(null)
            return
        }

        // assume for example that the music catalog is already loaded/cached

        val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()

        // check if this is the root menu
        if (MY_MEDIA_ROOT_ID == parentId) {
            // build the MediaItem objects for the top level,
            // and put the in the mediaItems list...
        } else {
            // examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems)
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        // player = null
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

    private fun buildNotification() {
        // get the session's metadata
        val controller = mediaSession.controller
        val mediaMetaData = controller.metadata
        val description = mediaMetaData.description
        if (myPlayerNotification == null) {
            val builder = NotificationCompat.Builder(
                baseContext,
                getString(R.string.media_playback_channel_id)
            ).apply {
                // add the metadata for the currently playing track
                setContentTitle(description.title)
                setContentText(description.subtitle)
                setSubText(description.description)
                setLargeIcon(description.iconBitmap)

                // enable launching the player by clicking the notification
                setContentIntent(controller.sessionActivity)

                // stop the service when the notification is swiped away
                setDeleteIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        baseContext,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )

                // make the transport controls visible on the lockscreen
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // add an app icon and set its accent color
                setSmallIcon(R.drawable.ic_launcher_foreground)
                color = ContextCompat.getColor(baseContext,
                    R.color.colorPrimaryDark
                )

                addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_round_skip_previous,
                        getString(R.string.skip_previous),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            baseContext,
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        )
                    )
                )
                addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_round_pause,
                        getString(R.string.pause),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            baseContext,
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                        )
                    )
                )
                addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_round_skip_next,
                        getString(R.string.skip_next),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            baseContext,
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        )
                    )
                )

                // take advantage of mediaStyle features
                setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(1)

                        // add a cancel button
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                baseContext,
                                PlaybackStateCompat.ACTION_STOP
                            )
                        )
                )
            }
            myPlayerNotification = builder.build()
        }
        // display the notification and place the service in the foreground
        startForeground(MEDIA_NOTIFICATION_ID, myPlayerNotification)
    }

    companion object {
        private const val TAG = "MediaPlaybackService"
        const val MEDIA_NOTIFICATION_ID = 10
    }
}

private class BecomingNoisyReceiver(
    context: Context,
    sessionToken: MediaSessionCompat.Token
) : BroadcastReceiver() {
    private val controller = MediaControllerCompat(context, sessionToken)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            controller.transportControls.pause()
        }
    }
}
