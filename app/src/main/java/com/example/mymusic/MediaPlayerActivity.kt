package com.example.mymusic

import android.content.ComponentName
import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_media_player.*

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var mediaController: MediaControllerCompat
    private val connectionCallBacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // get the token for the MediaSession
            mediaBrowser.sessionToken.also { token ->
                // create a mediaControllerCompat
                val mediaController = MediaControllerCompat(
                    this@MediaPlayerActivity,
                    token
                )

                // save the controller
                MediaControllerCompat.setMediaController(this@MediaPlayerActivity, mediaController)
            }

            // finish building the UI
            buildTransportControls()
        }

        override fun onConnectionSuspended() {
            // the service has crashed. Disable transport controls until it automatically reconnects
        }

        override fun onConnectionFailed() {
            // the service has refused our connection
        }
    }
    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            TODO("implement")
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            TODO("implement")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        btnStartService.setOnClickListener {
        }
        // constructs a MediaBrowserCompat. Pass in the name of your MediaBrowserService
        // and the MediaBrowserCompat.ConnectionCallback that you've defined.
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlaybackService::class.java),
            connectionCallBacks,
            null
        )
    }

    override fun onStart() {
        super.onStart()
        // connects to the MediaBrowserService. Here's where the magic of
        // MediaBrowserCompat.ConnectionCallback comes in. If the connection is successful,
        // the onConnect() callback creates the media controller, links it to the media session,
        // links your UI controls to the MediaController, and registers the controller to receive
        // callbacks from the media session.
        mediaBrowser.connect()
    }

    override fun onResume() {
        super.onResume()
        // set the audio stream so your app responds to the volume control on the device
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        // disconnects your MediaBrowser and unregisters the MediaController.Callback when
        // your activity stops
        // todo see "stay in sync with the MediaSession
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    fun buildTransportControls() {
        mediaController = MediaControllerCompat.getMediaController(this)
        // grab the view for the play/pause button
        playPause.setOnClickListener {
            // since this is a play/pause button, you'll need to test the current state
            // and choose the action accordingly
            val pbState = mediaController.playbackState.state
            if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                mediaController.transportControls.pause()
            } else {
                mediaController.transportControls.play()
            }
        }

        // todo display the initial state
        val metadata = mediaController.metadata
        val pbState = mediaController.playbackState

        // register a callback the stay in sync
        mediaController.registerCallback(controllerCallback)
    }

    fun retrieveMediaFromContentResolver() {
        val resolver: ContentResolver = contentResolver
        val uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = resolver.query(uri, null, null, null, null)
        when {
            cursor == null -> {
                // query failed, handle error.
            }
            !cursor.moveToFirst() -> {
                // no media on the device
            }
            else -> {
                val titleColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
                val idColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
                do {
                    val thisId = cursor.getLong(idColumn)
                    val thisTitle = cursor.getString(titleColumn)
                    // ...process entry...
                } while (cursor.moveToNext())
            }
        }
        cursor?.close()

        // to use with the MediaPlayer
        val id: Long = 0 // retrieve it from somewhere
        val contentUri: Uri = ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            id
        )

        val mediaPlayer = MediaPlayer().apply {
            setDataSource(applicationContext, contentUri)
        }
    }
}
//val pendingIntent: PendingIntent =
//         Intent(this, ExampleActivity::class.java).let { notificationIntent ->
//             PendingIntent.getActivity(this, 0, notificationIntent, 0)
//         }
//
// val notification: Notification = Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
//         .setContentTitle(getText(R.string.notification_title))
//         .setContentText(getText(R.string.notification_message))
//         .setSmallIcon(R.drawable.icon)
//         .setContentIntent(pendingIntent)
//         .setTicker(getText(R.string.ticker_text))
//         .build()
//
// startForeground(ONGOING_NOTIFICATION_ID, notification)
